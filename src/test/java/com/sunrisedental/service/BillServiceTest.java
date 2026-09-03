package com.sunrisedental.service;

import com.sunrisedental.domain.Appointment;
import com.sunrisedental.domain.Bill;
import com.sunrisedental.fakes.*;
import com.sunrisedental.patterns.NotificationCenter;
import com.sunrisedental.util.ServiceResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/** Realises the "Calculate and Print Bill" sequence diagram (Task A Figure 5) as automated tests. */
class BillServiceTest {

    private AppointmentService appointmentService;
    private BillService billService;
    private String appointmentNumber;

    @BeforeEach
    void setUp() {
        FakePatientDAO patientDAO = new FakePatientDAO();
        FakeDentistDAO dentistDAO = new FakeDentistDAO().withSeedData();
        FakeTreatmentTypeDAO treatmentDAO = new FakeTreatmentTypeDAO().withSeedData(); // treatment 1 = Rs. 1000.00
        FakeAppointmentDAO appointmentDAO = new FakeAppointmentDAO();
        FakeBillDAO billDAO = new FakeBillDAO();

        appointmentService = new AppointmentService(patientDAO, dentistDAO, treatmentDAO, appointmentDAO, new NotificationCenter());
        billService = new BillService(appointmentService, billDAO);

        AppointmentRegistrationRequest req = new AppointmentRegistrationRequest(
            "Kasun Perera", "12 Galle Rd, Colombo", "0771234567", 1, 1,
            LocalDate.now().plusDays(1).toString(), "10:00");
        appointmentNumber = appointmentService.registerAppointment(req).getData().getAppointmentNumber();
    }

    @Test
    void billTotalsConsultationFeePlusTreatmentCostWithNoDiscount() {
        ServiceResult<Bill> result = billService.generateBill(appointmentNumber, 0.0);
        assertTrue(result.isSuccess());
        // consultation fee 1500.00 + treatment cost 1000.00 (Dental Check-up, see FakeTreatmentTypeDAO)
        assertEquals(2500.00, result.getData().getTotalAmount(), 0.001);
    }

    @Test
    void discountIsSubtractedFromTheTotal() {
        ServiceResult<Bill> result = billService.generateBill(appointmentNumber, 500.0);
        assertTrue(result.isSuccess());
        assertEquals(2000.00, result.getData().getTotalAmount(), 0.001);
    }

    @Test
    void negativeDiscountIsRejected() {
        ServiceResult<Bill> result = billService.generateBill(appointmentNumber, -100.0);
        assertFalse(result.isSuccess());
    }

    @Test
    void discountLargerThanTheBillIsRejected() {
        ServiceResult<Bill> result = billService.generateBill(appointmentNumber, 999999.0);
        assertFalse(result.isSuccess());
    }

    @Test
    void billingAnUnknownAppointmentNumberFails() {
        ServiceResult<Bill> result = billService.generateBill("APT-DOES-NOT-EXIST", 0.0);
        assertFalse(result.isSuccess());
    }

    @Test
    void generatingTheSameBillTwiceReturnsTheExistingBillRatherThanDuplicating() {
        Bill first = billService.generateBill(appointmentNumber, 0.0).getData();
        Bill second = billService.generateBill(appointmentNumber, 0.0).getData();
        assertEquals(first.getBillId(), second.getBillId());
    }

    @Test
    void receiptTextContainsTheAppointmentNumberAndTotal() {
        Bill bill = billService.generateBill(appointmentNumber, 0.0).getData();
        String receipt = bill.printReceipt();
        assertTrue(receipt.contains(appointmentNumber));
        assertTrue(receipt.contains("2500.00"));
    }
}
