package com.sunrisedental.server;

import com.sun.net.httpserver.HttpServer;
import com.sunrisedental.dao.*;
import com.sunrisedental.patterns.EmailSmsNotifier;
import com.sunrisedental.patterns.NotificationCenter;
import com.sunrisedental.service.AppointmentService;
import com.sunrisedental.service.AuthenticationService;
import com.sunrisedental.service.BillService;
import com.sunrisedental.service.ReportService;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * Entry point for the "web services" half of the distributed architecture.
 * Run with: mvn compile exec:java -Dexec.mainClass=com.sunrisedental.server.ApiServerApp
 * (or run the class directly once mysql-connector-j is on the classpath, e.g. via IntelliJ).
 *
 * Listens on port 8080 by default (override with -Dserver.port=NNNN).
 * The console client (ConsoleClientApp) is a completely separate JVM
 * process that talks to this server only over HTTP - this is what
 * satisfies the brief's "distributed application with web services"
 * requirement: client and server can run on different machines, and
 * multiple receptionist workstations can share the one server + database.
 */
public class ApiServerApp {

    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(System.getProperty("server.port", "8080"));

        // ---- DAO layer (DAO pattern - concrete MySQL implementations) ----
        IUserDAO userDAO = new UserDAOImpl();
        IPatientDAO patientDAO = new PatientDAOImpl();
        IDentistDAO dentistDAO = new DentistDAOImpl();
        ITreatmentTypeDAO treatmentTypeDAO = new TreatmentTypeDAOImpl();
        IAppointmentDAO appointmentDAO = new AppointmentDAOImpl();
        IBillDAO billDAO = new BillDAOImpl();

        // ---- Observer pattern: subscribe the (simulated) email/SMS notifier ----
        NotificationCenter notificationCenter = new NotificationCenter();
        notificationCenter.subscribe(new EmailSmsNotifier());

        // ---- Service layer (Facade pattern) ----
        AuthenticationService authService = new AuthenticationService(userDAO);
        AppointmentService appointmentService = new AppointmentService(
            patientDAO, dentistDAO, treatmentTypeDAO, appointmentDAO, notificationCenter);
        BillService billService = new BillService(appointmentService, billDAO);
        ReportService reportService = new ReportService(appointmentDAO, billDAO);

        // ---- HTTP server + routing ----
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/api/login", new AuthHandler(authService));
        server.createContext("/api/appointments", new AppointmentHandler(appointmentService));
        server.createContext("/api/bills/", new BillHandler(billService));
        server.createContext("/api/reports/", new ReportHandler(reportService));
        server.createContext("/api/dentists", new ReferenceDataHandler(dentistDAO, treatmentTypeDAO));
        server.createContext("/api/treatments", new ReferenceDataHandler(dentistDAO, treatmentTypeDAO));
        server.setExecutor(Executors.newFixedThreadPool(8));

        server.start();
        System.out.println("Sunrise Dental Clinic API server listening on http://localhost:" + port);
        System.out.println("Endpoints: POST /api/login, POST /api/appointments, GET /api/appointments/{number},");
        System.out.println("           POST /api/appointments/{number}/reschedule, POST /api/appointments/{number}/cancel,");
        System.out.println("           POST /api/bills/{number}, GET /api/reports/daily, GET /api/reports/revenue,");
        System.out.println("           GET /api/dentists, GET /api/treatments");
    }
}
