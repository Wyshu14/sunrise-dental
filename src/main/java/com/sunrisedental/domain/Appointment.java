package com.sunrisedental.domain;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Central domain class: links a Patient, a Dentist and a TreatmentType.
 * Matches the Appointment class in the Task A class diagram, including
 * the auto-generated, unique appointmentNumber (see Section 4.2 of the
 * Task A sequence diagrams).
 */
public class Appointment {

    private int id;                 // internal database primary key
    private String appointmentNumber; // unique, staff-facing identifier, e.g. APT-20260903-0001
    private Patient patient;
    private Dentist dentist;
    private TreatmentType treatmentType;
    private LocalDate appointmentDate;
    private LocalTime appointmentTime;
    private AppointmentStatus status;

    public Appointment(int id, String appointmentNumber, Patient patient, Dentist dentist,
                        TreatmentType treatmentType, LocalDate appointmentDate, LocalTime appointmentTime,
                        AppointmentStatus status) {
        this.id = id;
        this.appointmentNumber = appointmentNumber;
        this.patient = patient;
        this.dentist = dentist;
        this.treatmentType = treatmentType;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.status = status;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getAppointmentNumber() { return appointmentNumber; }
    public void setAppointmentNumber(String appointmentNumber) { this.appointmentNumber = appointmentNumber; }

    public Patient getPatient() { return patient; }
    public Dentist getDentist() { return dentist; }
    public TreatmentType getTreatmentType() { return treatmentType; }

    public LocalDate getAppointmentDate() { return appointmentDate; }
    public LocalTime getAppointmentTime() { return appointmentTime; }

    public AppointmentStatus getStatus() { return status; }

    public void updateStatus(AppointmentStatus status) {
        this.status = status;
    }

    public void reschedule(LocalDate newDate, LocalTime newTime) {
        this.appointmentDate = newDate;
        this.appointmentTime = newTime;
        this.status = AppointmentStatus.RESCHEDULED;
    }

    @Override
    public String toString() {
        return String.format(
            "Appointment %s | %s | Patient: %s | Dentist: %s | Treatment: %s | %s %s | Status: %s",
            appointmentNumber, id, patient.getName(), dentist.getName(),
            treatmentType.getTreatmentName(), appointmentDate, appointmentTime, status
        );
    }
}
