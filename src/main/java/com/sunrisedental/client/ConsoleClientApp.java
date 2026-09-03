package com.sunrisedental.client;

import com.sunrisedental.util.Json;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Menu-driven console client - the user-facing half of the brief's
 * "menu driven application" requirement. It is a completely separate
 * program from ApiServerApp; every action it takes is a network call.
 * Run with: mvn compile exec:java -Dexec.mainClass=com.sunrisedental.client.ConsoleClientApp
 */
public class ConsoleClientApp {

    private final Scanner in = new Scanner(System.in);
    private final ApiClient api;
    private String loggedInRole;
    private String loggedInUsername;

    public ConsoleClientApp(String serverUrl) {
        this.api = new ApiClient(serverUrl);
    }

    public static void main(String[] args) {
        String serverUrl = args.length > 0 ? args[0] : "http://localhost:8080";
        new ConsoleClientApp(serverUrl).run();
    }

    public void run() {
        printBanner();
        if (!login()) {
            System.out.println("Too many failed attempts. Exiting.");
            return;
        }
        boolean running = true;
        while (running) {
            printMenu();
            String choice = prompt("Choose an option: ");
            switch (choice) {
                case "1" -> registerAppointment();
                case "2" -> searchAppointment();
                case "3" -> rescheduleAppointment();
                case "4" -> cancelAppointment();
                case "5" -> calculateAndPrintBill();
                case "6" -> viewHelp();
                case "7" -> {
                    if ("ADMINISTRATOR".equals(loggedInRole)) {
                        generateReports();
                    } else {
                        running = false;
                        System.out.println("Goodbye, " + loggedInUsername + ".");
                    }
                }
                case "8" -> {
                    if ("ADMINISTRATOR".equals(loggedInRole)) {
                        running = false;
                        System.out.println("Goodbye, " + loggedInUsername + ".");
                    } else {
                        System.out.println("Invalid option.");
                    }
                }
                default -> System.out.println("Invalid option, please choose again.");
            }
        }
    }

    // ---------------- 1. Login ----------------

    private boolean login() {
        for (int attempt = 1; attempt <= 3; attempt++) {
            String username = prompt("Username: ");
            String password = prompt("Password: ");
            ApiResponse resp = api.post("/api/login", Json.obj("username", username, "password", password));

            if (resp.isConnectionError()) {
                System.out.println("Could not reach the server: " + resp.getConnectionErrorMessage());
                System.out.println("Is ApiServerApp running? (mvn compile exec:java -Dexec.mainClass=com.sunrisedental.server.ApiServerApp)");
                continue;
            }
            if (resp.isSuccess()) {
                Map<String, Object> user = resp.getData("user");
                loggedInUsername = (String) user.get("username");
                loggedInRole = (String) user.get("role");
                System.out.println("\nWelcome, " + loggedInUsername + " (" + loggedInRole + ")!\n");
                return true;
            } else {
                System.out.println("Login failed: " + String.join("; ", resp.getErrors()));
                System.out.println("Attempt " + attempt + " of 3.\n");
            }
        }
        return false;
    }

    // ---------------- 2. Register New Appointment ----------------

    private void registerAppointment() {
        System.out.println("\n-- Register New Appointment --");

        System.out.println("Available dentists:");
        ApiResponse dentistsResp = api.get("/api/dentists");
        if (!printListOrBail(dentistsResp, "dentists", "dentistId", "name")) return;

        System.out.println("Available treatment types:");
        ApiResponse treatmentsResp = api.get("/api/treatments");
        if (!printListOrBail(treatmentsResp, "treatments", "treatmentId", "treatmentName")) return;

        String name = prompt("Patient name: ");
        String address = prompt("Address: ");
        String contact = prompt("Contact number (e.g. 0771234567): ");
        int dentistId = promptInt("Dentist ID: ");
        int treatmentId = promptInt("Treatment type ID: ");
        String date = prompt("Appointment date (YYYY-MM-DD): ");
        String time = prompt("Appointment time (HH:MM, 24hr): ");

        ApiResponse resp = api.post("/api/appointments", Json.obj(
            "patientName", name, "address", address, "contactNumber", contact,
            "dentistId", dentistId, "treatmentId", treatmentId, "date", date, "time", time
        ));

        if (resp.isConnectionError()) {
            System.out.println("Could not reach the server: " + resp.getConnectionErrorMessage());
        } else if (resp.isSuccess()) {
            Map<String, Object> appt = resp.getData("appointment");
            System.out.println("\nAppointment registered successfully!");
            System.out.println("Appointment Number: " + appt.get("appointmentNumber"));
            printAppointment(appt);
        } else {
            System.out.println("\nCould not register appointment:");
            resp.getErrors().forEach(e -> System.out.println("  - " + e));
        }
    }

    // ---------------- 3. Display Appointment Details ----------------

    private void searchAppointment() {
        System.out.println("\n-- Search / Display Appointment --");
        String number = prompt("Appointment number: ");
        ApiResponse resp = api.get("/api/appointments/" + number);
        if (resp.isConnectionError()) {
            System.out.println("Could not reach the server: " + resp.getConnectionErrorMessage());
        } else if (resp.isSuccess()) {
            printAppointment(resp.getData("appointment"));
        } else {
            System.out.println("Not found: " + String.join("; ", resp.getErrors()));
        }
    }

    private void rescheduleAppointment() {
        System.out.println("\n-- Update / Reschedule Appointment --");
        String number = prompt("Appointment number: ");
        String date = prompt("New date (YYYY-MM-DD): ");
        String time = prompt("New time (HH:MM): ");
        ApiResponse resp = api.post("/api/appointments/" + number + "/reschedule", Json.obj("date", date, "time", time));
        if (resp.isConnectionError()) {
            System.out.println("Could not reach the server: " + resp.getConnectionErrorMessage());
        } else if (resp.isSuccess()) {
            System.out.println("Appointment rescheduled.");
            printAppointment(resp.getData("appointment"));
        } else {
            resp.getErrors().forEach(e -> System.out.println("  - " + e));
        }
    }

    private void cancelAppointment() {
        System.out.println("\n-- Cancel Appointment --");
        String number = prompt("Appointment number: ");
        String confirm = prompt("Type YES to confirm cancellation: ");
        if (!"YES".equalsIgnoreCase(confirm)) {
            System.out.println("Cancellation aborted.");
            return;
        }
        ApiResponse resp = api.post("/api/appointments/" + number + "/cancel", Map.of());
        if (resp.isConnectionError()) {
            System.out.println("Could not reach the server: " + resp.getConnectionErrorMessage());
        } else if (resp.isSuccess()) {
            System.out.println("Appointment " + number + " cancelled.");
        } else {
            resp.getErrors().forEach(e -> System.out.println("  - " + e));
        }
    }

    // ---------------- 4. Calculate and Print Bill ----------------

    private void calculateAndPrintBill() {
        System.out.println("\n-- Calculate & Print Bill --");
        String number = prompt("Appointment number: ");
        String discountStr = prompt("Discount / insurance cover amount (press Enter for none): ");
        double discount = discountStr.isBlank() ? 0.0 : Double.parseDouble(discountStr);

        ApiResponse resp = api.post("/api/bills/" + number, Json.obj("discountAmount", discount));
        if (resp.isConnectionError()) {
            System.out.println("Could not reach the server: " + resp.getConnectionErrorMessage());
        } else if (resp.isSuccess()) {
            Map<String, Object> bill = resp.getData("bill");
            System.out.println("\n" + bill.get("receiptText"));
        } else {
            System.out.println("Could not generate bill:");
            resp.getErrors().forEach(e -> System.out.println("  - " + e));
        }
    }

    // ---------------- 5. Help ----------------

    private void viewHelp() {
        System.out.println("""

            ================= HELP: HOW TO USE THIS SYSTEM =================
            1. LOGIN
               Enter the username and password given to you by the clinic
               administrator. You have 3 attempts before the program exits.

            2. REGISTER NEW APPOINTMENT
               Choose this option, note down a dentist ID and a treatment
               ID from the lists shown, then enter the patient's details.
               The system will show you the new appointment number - write
               it down or give it to the patient, since it is needed to
               look the appointment up again later.

            3. SEARCH / DISPLAY APPOINTMENT
               Enter an appointment number to see its full details.

            4. UPDATE / RESCHEDULE APPOINTMENT
               Enter the appointment number plus a new date and time.

            5. CANCEL APPOINTMENT
               Enter the appointment number and confirm with YES.
               Cancelled appointments are kept on record, not deleted.

            6. CALCULATE & PRINT BILL
               Enter the appointment number. If the patient has a discount
               or insurance cover, enter the amount to deduct, otherwise
               press Enter. A receipt is generated and shown on screen.

            7. (Administrators only) GENERATE REPORTS
               View today's appointment list or a revenue report for a
               date range.

            If the program cannot reach the server, make sure
            ApiServerApp is running and that you are using the correct
            server address.
            ==================================================================
            """);
    }

    // ---------------- Administrator: Generate Reports ----------------

    private void generateReports() {
        System.out.println("\n-- Generate Reports --");
        System.out.println("1. Daily appointments report");
        System.out.println("2. Revenue report");
        String choice = prompt("Choose a report: ");
        if ("1".equals(choice)) {
            String date = prompt("Date (YYYY-MM-DD, press Enter for today): ");
            String path = date.isBlank() ? "/api/reports/daily" : "/api/reports/daily?date=" + date;
            ApiResponse resp = api.get(path);
            if (resp.isSuccess()) System.out.println("\n" + resp.getString("report"));
            else System.out.println("Could not generate report.");
        } else if ("2".equals(choice)) {
            String from = prompt("From date (YYYY-MM-DD): ");
            String to = prompt("To date (YYYY-MM-DD): ");
            ApiResponse resp = api.get("/api/reports/revenue?from=" + from + "&to=" + to);
            if (resp.isSuccess()) System.out.println("\n" + resp.getString("report"));
            else System.out.println("Could not generate report.");
        } else {
            System.out.println("Invalid option.");
        }
    }

    // ---------------- helpers ----------------

    private void printBanner() {
        System.out.println("""
            ==================================================================
                 SUNRISE DENTAL CLINIC - Appointment & Patient Management
            ==================================================================
            """);
    }

    private void printMenu() {
        System.out.println("\n---------------------------------------------");
        System.out.println("1. Register New Appointment");
        System.out.println("2. Search / Display Appointment");
        System.out.println("3. Update / Reschedule Appointment");
        System.out.println("4. Cancel Appointment");
        System.out.println("5. Calculate & Print Bill");
        System.out.println("6. Help");
        if ("ADMINISTRATOR".equals(loggedInRole)) {
            System.out.println("7. Generate Reports");
            System.out.println("8. Exit");
        } else {
            System.out.println("7. Exit");
        }
        System.out.println("---------------------------------------------");
    }

    private void printAppointment(Map<String, Object> appt) {
        System.out.println("  Appointment No : " + appt.get("appointmentNumber"));
        System.out.println("  Patient        : " + appt.get("patientName"));
        System.out.println("  Address        : " + appt.get("patientAddress"));
        System.out.println("  Contact        : " + appt.get("patientContact"));
        System.out.println("  Dentist        : " + appt.get("dentistName"));
        System.out.println("  Treatment      : " + appt.get("treatmentType"));
        System.out.println("  Date / Time    : " + appt.get("date") + " " + appt.get("time"));
        System.out.println("  Status         : " + appt.get("status"));
    }

    private boolean printListOrBail(ApiResponse resp, String key, String idField, String nameField) {
        if (resp.isConnectionError()) {
            System.out.println("Could not reach the server: " + resp.getConnectionErrorMessage());
            return false;
        }
        if (!resp.isSuccess()) {
            System.out.println("Could not load reference data.");
            return false;
        }
        List<Map<String, Object>> items = resp.getList(key);
        for (Map<String, Object> item : items) {
            System.out.println("  " + item.get(idField) + " - " + item.get(nameField));
        }
        return true;
    }

    private String prompt(String label) {
        System.out.print(label);
        return in.nextLine().trim();
    }

    private int promptInt(String label) {
        while (true) {
            String value = prompt(label);
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a whole number.");
            }
        }
    }
}
