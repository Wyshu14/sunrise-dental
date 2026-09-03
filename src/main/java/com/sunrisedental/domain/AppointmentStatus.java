package com.sunrisedental.domain;

/** Status field on Appointment. Cancelling/rescheduling updates this rather than deleting the row (see Task A assumptions). */
public enum AppointmentStatus {
    BOOKED,
    RESCHEDULED,
    CANCELLED,
    COMPLETED
}
