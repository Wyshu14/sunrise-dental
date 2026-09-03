package com.sunrisedental.patterns;

import com.sunrisedental.domain.*;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * BUILDER PATTERN.
 *
 * Why: Appointment has several required collaborators (patient, dentist,
 * treatment type, date, time) that must all be supplied before the object
 * is valid, plus fields with sensible defaults (id, status). A telescoping
 * constructor would be error-prone to call correctly; this fluent builder
 * makes the registration code in AppointmentService read like a sentence
 * and makes it hard to forget a required field.
 */
public class AppointmentBuilder {

    private String appointmentNumber;
    private Patient patient;
    private Dentist dentist;
    private TreatmentType treatmentType;
    private LocalDate date;
    private LocalTime time;
    private AppointmentStatus status = AppointmentStatus.BOOKED;

    public AppointmentBuilder withAppointmentNumber(String number) {
        this.appointmentNumber = number;
        return this;
    }

    public AppointmentBuilder withPatient(Patient patient) {
        this.patient = patient;
        return this;
    }

    public AppointmentBuilder withDentist(Dentist dentist) {
        this.dentist = dentist;
        return this;
    }

    public AppointmentBuilder withTreatmentType(TreatmentType treatmentType) {
        this.treatmentType = treatmentType;
        return this;
    }

    public AppointmentBuilder onDate(LocalDate date) {
        this.date = date;
        return this;
    }

    public AppointmentBuilder atTime(LocalTime time) {
        this.time = time;
        return this;
    }

    public AppointmentBuilder withStatus(AppointmentStatus status) {
        this.status = status;
        return this;
    }

    public Appointment build() {
        if (patient == null || dentist == null || treatmentType == null || date == null || time == null) {
            throw new IllegalStateException("Cannot build an Appointment without patient, dentist, treatment type, date and time");
        }
        // id (0 = not yet persisted) is assigned by the database on save
        return new Appointment(0, appointmentNumber, patient, dentist, treatmentType, date, time, status);
    }
}
