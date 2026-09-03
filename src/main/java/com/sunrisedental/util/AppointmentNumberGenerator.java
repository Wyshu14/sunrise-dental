package com.sunrisedental.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Generates unique, human-readable appointment numbers such as
 * APT-20260903-0007. The visible sequence resets each day for readability;
 * uniqueness is still guaranteed at the database level by a UNIQUE
 * constraint on appointments.appointment_number (see schema.sql), which is
 * the real safeguard against the "double bookings" problem named in the brief.
 */
public final class AppointmentNumberGenerator {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final AtomicInteger COUNTER = new AtomicInteger(0);

    private AppointmentNumberGenerator() { }

    public static String next() {
        String datePart = LocalDate.now().format(DATE_FMT);
        int seq = COUNTER.incrementAndGet();
        return String.format("APT-%s-%04d", datePart, seq);
    }
}
