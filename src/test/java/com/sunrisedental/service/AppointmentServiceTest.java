package com.sunrisedental.service;

import com.sunrisedental.domain.Appointment;
import com.sunrisedental.domain.AppointmentStatus;
import com.sunrisedental.fakes.*;
import com.sunrisedental.patterns.NotificationCenter;
import com.sunrisedental.util.ServiceResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Realises the "Register New Appointment" and related sequence diagrams
 * (Task A Figure 4) as automated tests, using in-memory fake DAOs so no
 * MySQL server is required to run this suite (see docs/test-plan.md).
 */
class AppointmentServiceTest {

    private AppointmentService appointmentService;
    private FakeAppointmentDAO appointmentDAO;

    @BeforeEach
    void setUp() {
        FakePatientDAO patientDAO = new FakePatientDAO();
        FakeDentistDAO dentistDAO = new FakeDentistDAO().withSeedData();
        FakeTreatmentTypeDAO treatmentDAO = new FakeTreatmentTypeDAO().withSeedData();
        appointmentDAO = new FakeAppointmentDAO();
        appointmentService = new AppointmentService(patientDAO, dentistDAO, treatmentDAO, appointmentDAO, new NotificationCenter());
    }

    private AppointmentRegistrationRequest validRequest() {
        return new AppointmentRegistrationRequest(
            "Kasun Perera", "12 Galle Rd, Colombo", "0771234567",
            1, 1, LocalDate.now().plusDays(1).toString(), "10:00");
    }

    @Test
    void validRequestRegistersSuccessfully() {
        ServiceResult<Appointment> result = appointmentService.registerAppointment(validRequest());
        assertTrue(result.isSuccess());
        assertNotNull(result.getData().getAppointmentNumber());
        assertEquals(AppointmentStatus.BOOKED, result.getData().getStatus());
    }

    @Test
    void invalidContactNumberIsRejectedAndNothingIsSaved() {
        AppointmentRegistrationRequest req = validRequest();
        req.contactNumber = "12345";
        ServiceResult<Appointment> result = appointmentService.registerAppointment(req);
        assertFalse(result.isSuccess());
        assertTrue(appointmentDAO.findAll().isEmpty(), "an invalid request must not reach the DAO");
    }

    @Test
    void unknownDentistIdIsRejected() {
        AppointmentRegistrationRequest req = validRequest();
        req.dentistId = 999;
        ServiceResult<Appointment> result = appointmentService.registerAppointment(req);
        assertFalse(result.isSuccess());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("dentist")));
    }

    @Test
    void doubleBookingTheSameDentistSlotIsRejected() {
        appointmentService.registerAppointment(validRequest());
        ServiceResult<Appointment> second = appointmentService.registerAppointment(validRequest());
        assertFalse(second.isSuccess(), "registering the same dentist/date/time twice must fail (brief: 'double bookings')");
    }

    @Test
    void sameDentistDifferentTimeIsAllowed() {
        appointmentService.registerAppointment(validRequest());
        AppointmentRegistrationRequest req2 = validRequest();
        req2.time = "11:00";
        assertTrue(appointmentService.registerAppointment(req2).isSuccess());
    }

    @Test
    void searchFindsARegisteredAppointmentByNumber() {
        Appointment registered = appointmentService.registerAppointment(validRequest()).getData();
        ServiceResult<Appointment> found = appointmentService.searchByNumber(registered.getAppointmentNumber());
        assertTrue(found.isSuccess());
        assertEquals("Kasun Perera", found.getData().getPatient().getName());
    }

    @Test
    void searchForUnknownAppointmentNumberFails() {
        ServiceResult<Appointment> result = appointmentService.searchByNumber("APT-DOES-NOT-EXIST");
        assertFalse(result.isSuccess());
    }

    @Test
    void rescheduleUpdatesDateTimeAndStatus() {
        Appointment registered = appointmentService.registerAppointment(validRequest()).getData();
        String newDate = LocalDate.now().plusDays(5).toString();
        ServiceResult<Appointment> result = appointmentService.reschedule(registered.getAppointmentNumber(), newDate, "15:00");
        assertTrue(result.isSuccess());
        assertEquals(AppointmentStatus.RESCHEDULED, result.getData().getStatus());
        assertEquals(newDate, result.getData().getAppointmentDate().toString());
    }

    @Test
    void cancelSetsStatusToCancelledRatherThanDeletingTheRecord() {
        Appointment registered = appointmentService.registerAppointment(validRequest()).getData();
        appointmentService.cancel(registered.getAppointmentNumber());

        ServiceResult<Appointment> found = appointmentService.searchByNumber(registered.getAppointmentNumber());
        assertTrue(found.isSuccess(), "a cancelled appointment must still be findable (audit trail assumption, Task A)");
        assertEquals(AppointmentStatus.CANCELLED, found.getData().getStatus());
    }

    @Test
    void cancellingFreesTheSlotForDoubleBookingPurposes() {
        Appointment registered = appointmentService.registerAppointment(validRequest()).getData();
        appointmentService.cancel(registered.getAppointmentNumber());

        ServiceResult<Appointment> reRegistered = appointmentService.registerAppointment(validRequest());
        assertTrue(reRegistered.isSuccess(), "the same slot must be bookable again once the original appointment is cancelled");
    }
}
