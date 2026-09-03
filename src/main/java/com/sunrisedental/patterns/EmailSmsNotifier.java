package com.sunrisedental.patterns;

import com.sunrisedental.domain.Appointment;

import java.util.logging.Logger;

/** A concrete Observer: simulates sending a confirmation email/SMS by logging it (see NotificationCenter for rationale). */
public class EmailSmsNotifier implements AppointmentEventListener {

    private static final Logger LOG = Logger.getLogger(EmailSmsNotifier.class.getName());

    @Override
    public void onAppointmentRegistered(Appointment appointment) {
        LOG.info(() -> String.format(
            "[SIMULATED EMAIL/SMS] To %s: Your appointment %s with %s on %s at %s is confirmed.",
            appointment.getPatient().getContactNumber(), appointment.getAppointmentNumber(),
            appointment.getDentist().getName(), appointment.getAppointmentDate(), appointment.getAppointmentTime()
        ));
    }

    @Override
    public void onAppointmentCancelled(Appointment appointment) {
        LOG.info(() -> String.format(
            "[SIMULATED EMAIL/SMS] To %s: Your appointment %s has been cancelled.",
            appointment.getPatient().getContactNumber(), appointment.getAppointmentNumber()
        ));
    }
}
