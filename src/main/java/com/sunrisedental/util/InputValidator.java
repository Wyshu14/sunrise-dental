package com.sunrisedental.util;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Central input validation used by both the server (defense against a
 * malicious/buggy client) and the console client (fast feedback before a
 * request is even sent). Directly implements the brief's requirement for
 * "proper validation mechanisms ... to restrict invalid entries", and is
 * the "Validate Appointment Data" use case «included» by
 * "Register New Appointment" in the Task A use case diagram.
 */
public final class InputValidator {

    private static final Pattern CONTACT_NUMBER = Pattern.compile("^0\\d{9}$"); // Sri Lankan local format, e.g. 0771234567
    private static final Pattern NAME = Pattern.compile("^[A-Za-z][A-Za-z .'-]{1,59}$");

    private InputValidator() { }

    public static List<String> validatePatientName(String name) {
        List<String> errors = new ArrayList<>();
        if (name == null || name.isBlank()) {
            errors.add("Patient name is required.");
        } else if (!NAME.matcher(name.trim()).matches()) {
            errors.add("Patient name must be 2-60 letters (spaces, apostrophes and hyphens allowed) and cannot start with a digit.");
        }
        return errors;
    }

    public static List<String> validateAddress(String address) {
        List<String> errors = new ArrayList<>();
        if (address == null || address.isBlank()) {
            errors.add("Address is required.");
        } else if (address.trim().length() > 120) {
            errors.add("Address must be 120 characters or fewer.");
        }
        return errors;
    }

    public static List<String> validateContactNumber(String contactNumber) {
        List<String> errors = new ArrayList<>();
        if (contactNumber == null || contactNumber.isBlank()) {
            errors.add("Contact number is required.");
        } else if (!CONTACT_NUMBER.matcher(contactNumber.trim()).matches()) {
            errors.add("Contact number must be 10 digits starting with 0 (e.g. 0771234567).");
        }
        return errors;
    }

    public static List<String> validateAppointmentDate(String dateStr) {
        List<String> errors = new ArrayList<>();
        try {
            LocalDate date = LocalDate.parse(dateStr);
            if (date.isBefore(LocalDate.now())) {
                errors.add("Appointment date cannot be in the past.");
            }
            if (date.isAfter(LocalDate.now().plusMonths(6))) {
                errors.add("Appointment date cannot be more than 6 months in the future.");
            }
        } catch (DateTimeParseException | NullPointerException e) {
            errors.add("Appointment date must be in YYYY-MM-DD format.");
        }
        return errors;
    }

    public static List<String> validateAppointmentTime(String timeStr) {
        List<String> errors = new ArrayList<>();
        try {
            LocalTime time = LocalTime.parse(timeStr);
            LocalTime open = LocalTime.of(8, 0);
            LocalTime close = LocalTime.of(20, 0);
            if (time.isBefore(open) || time.isAfter(close)) {
                errors.add("Appointment time must be between 08:00 and 20:00 (clinic hours).");
            }
        } catch (DateTimeParseException | NullPointerException e) {
            errors.add("Appointment time must be in HH:MM (24-hour) format.");
        }
        return errors;
    }

    public static List<String> validateLoginInput(String username, String password) {
        List<String> errors = new ArrayList<>();
        if (username == null || username.isBlank()) errors.add("Username is required.");
        if (password == null || password.isBlank()) errors.add("Password is required.");
        return errors;
    }
}
