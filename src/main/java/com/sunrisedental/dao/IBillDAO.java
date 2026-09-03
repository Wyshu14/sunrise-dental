package com.sunrisedental.dao;

import com.sunrisedental.domain.Bill;
import java.util.List;
import java.util.Optional;

public interface IBillDAO {
    Bill save(Bill bill);
    Optional<Bill> findByAppointmentNumber(String appointmentNumber);
    List<Bill> findAll();
}
