package com.sunrisedental;

import com.sun.net.httpserver.HttpServer;
import com.sunrisedental.client.ApiClient;
import com.sunrisedental.client.ApiResponse;
import com.sunrisedental.fakes.*;
import com.sunrisedental.patterns.EmailSmsNotifier;
import com.sunrisedental.patterns.NotificationCenter;
import com.sunrisedental.patterns.UserFactory;
import com.sunrisedental.server.*;
import com.sunrisedental.service.AppointmentService;
import com.sunrisedental.service.AuthenticationService;
import com.sunrisedental.service.BillService;
import com.sunrisedental.util.Json;

import java.net.InetSocketAddress;
import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * End-to-end smoke test that runs the REAL HTTP server and REAL console-client
 * networking code, wired to in-memory fake DAOs instead of MySQL (which is
 * not installable in this sandbox - see README "Known limitation").
 * This is not a substitute for the JUnit suite in src/test/java (Task C) -
 * it is a self-contained way to prove, right now, that the domain model,
 * design patterns, service layer, JSON (de)serialisation, HTTP routing and
 * console-client networking code all actually work together, not just compile.
 *
 * Run: java -cp out-test com.sunrisedental.SmokeTest
 */
public class SmokeTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) throws Exception {
        FakeUserDAO userDAO = new FakeUserDAO();
        FakePatientDAO patientDAO = new FakePatientDAO();
        FakeDentistDAO dentistDAO = new FakeDentistDAO().withSeedData();
        FakeTreatmentTypeDAO treatmentDAO = new FakeTreatmentTypeDAO().withSeedData();
        FakeAppointmentDAO appointmentDAO = new FakeAppointmentDAO();
        FakeBillDAO billDAO = new FakeBillDAO();

        userDAO.save(UserFactory.createUser(1, "reception1", "Reception@123", "Nadeesha Perera", "RECEPTIONIST"));

        NotificationCenter notificationCenter = new NotificationCenter();
        notificationCenter.subscribe(new EmailSmsNotifier());

        AuthenticationService authService = new AuthenticationService(userDAO);
        AppointmentService appointmentService = new AppointmentService(patientDAO, dentistDAO, treatmentDAO, appointmentDAO, notificationCenter);
        BillService billService = new BillService(appointmentService, billDAO);

        int port = 8099;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/api/login", new AuthHandler(authService));
        server.createContext("/api/appointments", new AppointmentHandler(appointmentService));
        server.createContext("/api/bills/", new BillHandler(billService));
        server.createContext("/api/dentists", new ReferenceDataHandler(dentistDAO, treatmentDAO));
        server.createContext("/api/treatments", new ReferenceDataHandler(dentistDAO, treatmentDAO));
        var executor = Executors.newFixedThreadPool(4);
        server.setExecutor(executor);
        server.start();
        System.out.println("Test server started on port " + port);

        try {
            ApiClient client = new ApiClient("http://localhost:" + port);

            // 1. Login: wrong password must fail with generic message
            ApiResponse badLogin = client.post("/api/login", Json.obj("username", "reception1", "password", "wrong"));
            check("Login rejects wrong password", !badLogin.isSuccess() && badLogin.getStatusCode() == 401);

            // 2. Login: correct credentials succeed
            ApiResponse goodLogin = client.post("/api/login", Json.obj("username", "reception1", "password", "Reception@123"));
            check("Login accepts correct credentials", goodLogin.isSuccess());
            check("Login returns role", "RECEPTIONIST".equals(goodLogin.getData("user").get("role")));

            // 3. Reference data
            ApiResponse dentists = client.get("/api/dentists");
            check("Dentist list returned", dentists.isSuccess() && dentists.getList("dentists").size() == 2);

            // 4. Register appointment: invalid data is rejected (bad contact number)
            ApiResponse badRegister = client.post("/api/appointments", Json.obj(
                "patientName", "Kasun Perera", "address", "12 Galle Rd, Colombo", "contactNumber", "12345",
                "dentistId", 1, "treatmentId", 1, "date", LocalDate.now().plusDays(1).toString(), "time", "10:00"
            ));
            check("Register rejects invalid contact number", !badRegister.isSuccess());

            // 5. Register appointment: valid data succeeds
            ApiResponse register = client.post("/api/appointments", Json.obj(
                "patientName", "Kasun Perera", "address", "12 Galle Rd, Colombo", "contactNumber", "0771234567",
                "dentistId", 1, "treatmentId", 1, "date", LocalDate.now().plusDays(1).toString(), "time", "10:00"
            ));
            check("Register accepts valid data", register.isSuccess());
            String appointmentNumber = (String) register.getData("appointment").get("appointmentNumber");
            check("Appointment number generated", appointmentNumber != null && appointmentNumber.startsWith("APT-"));

            // 6. Double-booking is rejected (same dentist, date, time)
            ApiResponse doubleBook = client.post("/api/appointments", Json.obj(
                "patientName", "Another Patient", "address", "1 Main St, Colombo", "contactNumber", "0779999999",
                "dentistId", 1, "treatmentId", 1, "date", LocalDate.now().plusDays(1).toString(), "time", "10:00"
            ));
            check("Double-booking is rejected", !doubleBook.isSuccess());

            // 7. Search / display
            ApiResponse search = client.get("/api/appointments/" + appointmentNumber);
            check("Search finds the registered appointment", search.isSuccess()
                && "Kasun Perera".equals(search.getData("appointment").get("patientName")));

            // 8. Reschedule
            ApiResponse reschedule = client.post("/api/appointments/" + appointmentNumber + "/reschedule",
                Json.obj("date", LocalDate.now().plusDays(2).toString(), "time", "11:00"));
            check("Reschedule succeeds", reschedule.isSuccess()
                && "RESCHEDULED".equals(reschedule.getData("appointment").get("status")));

            // 9. Calculate & print bill
            ApiResponse bill = client.post("/api/bills/" + appointmentNumber, Json.obj("discountAmount", 500.0));
            check("Bill generated", bill.isSuccess());
            double total = (double) (Double) bill.getData("bill").get("totalAmount");
            check("Bill total = consultation(1500) + treatment(1000) - discount(500) = 2000",
                Math.abs(total - 2000.0) < 0.01);

            // 10. Cancel a different (unbooked) case: cancel this appointment, then confirm status
            ApiResponse cancel = client.post("/api/appointments/" + appointmentNumber + "/cancel", Map.of());
            check("Cancel succeeds", cancel.isSuccess() && "CANCELLED".equals(cancel.getData("appointment").get("status")));

            // 11. Unknown appointment number returns a clean 400, not a crash
            ApiResponse notFound = client.get("/api/appointments/DOES-NOT-EXIST");
            check("Unknown appointment number handled gracefully", !notFound.isSuccess());

        } finally {
            server.stop(0);
            executor.shutdownNow();
        }

        System.out.println();
        System.out.println("=====================================");
        System.out.println("SMOKE TEST RESULT: " + passed + " passed, " + failed + " failed");
        System.out.println("=====================================");
        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void check(String description, boolean condition) {
        if (condition) {
            passed++;
            System.out.println("[PASS] " + description);
        } else {
            failed++;
            System.out.println("[FAIL] " + description);
        }
    }
}
