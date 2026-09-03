package com.sunrisedental.service;

/**
 * DTO carrying the raw, unvalidated input for "Register New Appointment"
 * from either the console client or an HTTP request body, before
 * AppointmentService.validate() and registerAppointment() process it.
 */
public class AppointmentRegistrationRequest {
    public String patientName;
    public String address;
    public String contactNumber;
    public int dentistId;
    public int treatmentId;
    public String date; // YYYY-MM-DD
    public String time; // HH:MM

    public AppointmentRegistrationRequest() { }

    public AppointmentRegistrationRequest(String patientName, String address, String contactNumber,
                                           int dentistId, int treatmentId, String date, String time) {
        this.patientName = patientName;
        this.address = address;
        this.contactNumber = contactNumber;
        this.dentistId = dentistId;
        this.treatmentId = treatmentId;
        this.date = date;
        this.time = time;
    }
}
