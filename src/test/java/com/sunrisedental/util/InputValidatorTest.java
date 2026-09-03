package com.sunrisedental.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Realises the "Validate Appointment Data" «include» use case from
 * Task A. Test data was deliberately chosen to cover boundary cases
 * (see docs/test-plan.md, Section 2) rather than only "happy path" values.
 */
class InputValidatorTest {

    // ---- contact number ----

    @Test
    void validSriLankanMobileNumberPasses() {
        assertTrue(InputValidator.validateContactNumber("0771234567").isEmpty());
    }

    @Test
    void contactNumberMissingLeadingZeroFails() {
        assertFalse(InputValidator.validateContactNumber("771234567").isEmpty());
    }

    @Test
    void contactNumberTooShortFails() {
        assertFalse(InputValidator.validateContactNumber("07712345").isEmpty());
    }

    @Test
    void blankContactNumberFails() {
        assertFalse(InputValidator.validateContactNumber("").isEmpty());
        assertFalse(InputValidator.validateContactNumber(null).isEmpty());
    }

    // ---- patient name ----

    @Test
    void ordinaryNamePasses() {
        assertTrue(InputValidator.validatePatientName("Kasun Perera").isEmpty());
    }

    @Test
    void nameStartingWithDigitFails() {
        assertFalse(InputValidator.validatePatientName("1Kasun").isEmpty());
    }

    @Test
    void singleCharacterNameFails() {
        assertFalse(InputValidator.validatePatientName("K").isEmpty());
    }

    @Test
    void nameWithApostropheAndHyphenPasses() {
        assertTrue(InputValidator.validatePatientName("Anne-Marie O'Brien").isEmpty());
    }

    // ---- appointment date ----

    @Test
    void todayIsAValidAppointmentDate() {
        assertTrue(InputValidator.validateAppointmentDate(LocalDate.now().toString()).isEmpty());
    }

    @Test
    void yesterdayIsRejected() {
        assertFalse(InputValidator.validateAppointmentDate(LocalDate.now().minusDays(1).toString()).isEmpty());
    }

    @Test
    void dateSevenMonthsAheadIsRejected() {
        assertFalse(InputValidator.validateAppointmentDate(LocalDate.now().plusMonths(7).toString()).isEmpty());
    }

    @Test
    void malformedDateStringIsRejected() {
        assertFalse(InputValidator.validateAppointmentDate("03/09/2026").isEmpty());
    }

    // ---- appointment time ----

    @Test
    void timeWithinClinicHoursPasses() {
        assertTrue(InputValidator.validateAppointmentTime("14:30").isEmpty());
    }

    @Test
    void timeBeforeOpeningIsRejected() {
        assertFalse(InputValidator.validateAppointmentTime("07:59").isEmpty());
    }

    @Test
    void timeAfterClosingIsRejected() {
        assertFalse(InputValidator.validateAppointmentTime("20:01").isEmpty());
    }

    @Test
    void boundaryOpeningTimeIsAccepted() {
        assertTrue(InputValidator.validateAppointmentTime("08:00").isEmpty());
    }

    @Test
    void boundaryClosingTimeIsAccepted() {
        assertTrue(InputValidator.validateAppointmentTime("20:00").isEmpty());
    }
}
