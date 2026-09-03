package com.sunrisedental.patterns;

import com.sunrisedental.domain.Appointment;

/** OBSERVER PATTERN: the "Observer" role. Implement this to react to appointment events without AppointmentService knowing who's listening. */
public interface AppointmentEventListener {
    void onAppointmentRegistered(Appointment appointment);
    void onAppointmentCancelled(Appointment appointment);
}
