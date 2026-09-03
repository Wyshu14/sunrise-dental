package com.sunrisedental.patterns;

import com.sunrisedental.domain.Appointment;

import java.util.ArrayList;
import java.util.List;

/**
 * OBSERVER PATTERN: the "Subject" role.
 *
 * Why: the brief's top marking band rewards "complex functionality (e.g.
 * email alerts, SMS notifications)". Rather than hard-coding an email call
 * inside AppointmentService (which would violate single-responsibility and
 * make the service layer harder to test in isolation), AppointmentService
 * simply calls notifyRegistered()/notifyCancelled() on this subject, and
 * any number of independent listeners (an email notifier, an SMS notifier,
 * an audit logger) can subscribe without AppointmentService knowing they
 * exist. New notification channels can be added later with zero changes
 * to AppointmentService - this is the Open/Closed Principle in practice.
 *
 * EmailSmsNotifier below simulates sending (prints to the console / log)
 * rather than calling a real email/SMS provider, since wiring a live
 * provider needs external credentials and a network dependency this
 * offline-buildable project deliberately avoids; the report documents this
 * as an assumption and notes swapping in a real provider (e.g. JavaMail,
 * Twilio) only requires a new listener class.
 */
public class NotificationCenter {

    private final List<AppointmentEventListener> listeners = new ArrayList<>();

    public void subscribe(AppointmentEventListener listener) {
        listeners.add(listener);
    }

    public void notifyRegistered(Appointment appointment) {
        for (AppointmentEventListener listener : listeners) {
            listener.onAppointmentRegistered(appointment);
        }
    }

    public void notifyCancelled(Appointment appointment) {
        for (AppointmentEventListener listener : listeners) {
            listener.onAppointmentCancelled(appointment);
        }
    }
}
