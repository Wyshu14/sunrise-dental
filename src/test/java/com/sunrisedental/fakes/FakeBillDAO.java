package com.sunrisedental.fakes;

import com.sunrisedental.dao.IBillDAO;
import com.sunrisedental.domain.Bill;

import java.util.*;

public class FakeBillDAO implements IBillDAO {

    private final Map<String, Bill> byAppointmentNumber = new LinkedHashMap<>();
    private int nextId = 1;

    @Override
    public Bill save(Bill bill) {
        Bill stored = new Bill(nextId++, bill.getAppointmentNumber(), bill.getConsultationFee(),
            bill.getTreatmentCost(), bill.getDiscountAmount(), bill.getGeneratedDate());
        byAppointmentNumber.put(stored.getAppointmentNumber(), stored);
        return stored;
    }

    @Override
    public Optional<Bill> findByAppointmentNumber(String appointmentNumber) {
        return Optional.ofNullable(byAppointmentNumber.get(appointmentNumber));
    }

    @Override
    public List<Bill> findAll() {
        return new ArrayList<>(byAppointmentNumber.values());
    }
}
