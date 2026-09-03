package com.sunrisedental.service;

import com.sunrisedental.dao.IBillDAO;
import com.sunrisedental.domain.Appointment;
import com.sunrisedental.domain.Bill;
import com.sunrisedental.util.ServiceResult;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Realises the "Calculate and Print Bill" sequence diagram (Task A, Figure 5),
 * including its «include» of Search Appointment (delegated to AppointmentService)
 * and its «extend» Apply Discount/Insurance Cover (the optional discountAmount parameter).
 */
public class BillService {

    private static final double CONSULTATION_FEE = 1500.00; // flat consultation fee, LKR

    private final AppointmentService appointmentService;
    private final IBillDAO billDAO;

    public BillService(AppointmentService appointmentService, IBillDAO billDAO) {
        this.appointmentService = appointmentService;
        this.billDAO = billDAO;
    }

    /** discountAmount = 0 when the optional «extend» use case does not apply. */
    public ServiceResult<Bill> generateBill(String appointmentNumber, double discountAmount) {
        ServiceResult<Appointment> appointmentResult = appointmentService.searchByNumber(appointmentNumber);
        if (!appointmentResult.isSuccess()) {
            return ServiceResult.fail(appointmentResult.getErrors());
        }

        Appointment appointment = appointmentResult.getData();

        Optional<Bill> existing = billDAO.findByAppointmentNumber(appointmentNumber);
        if (existing.isPresent()) {
            return ServiceResult.ok(existing.get());
        }

        double treatmentCost = appointment.getTreatmentType().getCost();
        if (discountAmount < 0) {
            return ServiceResult.fail(List.of("Discount amount cannot be negative."));
        }
        if (discountAmount > CONSULTATION_FEE + treatmentCost) {
            return ServiceResult.fail(List.of("Discount cannot exceed the total bill amount."));
        }

        Bill bill = new Bill(0, appointmentNumber, CONSULTATION_FEE, treatmentCost, discountAmount, LocalDate.now());
        Bill saved = billDAO.save(bill);
        return ServiceResult.ok(saved);
    }
}
