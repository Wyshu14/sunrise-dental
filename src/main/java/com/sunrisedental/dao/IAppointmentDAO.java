package com.sunrisedental.dao;

import com.sunrisedental.domain.Appointment;
import java.util.List;
import java.util.Optional;

/** DAO PATTERN - interface half for Appointment persistence. See Task A Figure 2 (IAppointmentDAO). */
public interface IAppointmentDAO {
    Appointment save(Appointment appointment);
    Optional<Appointment> findByNumber(String appointmentNumber);
    List<Appointment> findAll();
    void update(Appointment appointment);
    boolean existsForDentistAtSlot(int dentistId, java.time.LocalDate date, java.time.LocalTime time);
}
