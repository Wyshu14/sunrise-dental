package com.sunrisedental.fakes;

import com.sunrisedental.dao.IAppointmentDAO;
import com.sunrisedental.domain.Appointment;
import com.sunrisedental.domain.AppointmentStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

public class FakeAppointmentDAO implements IAppointmentDAO {

    private final Map<String, Appointment> byNumber = new LinkedHashMap<>();
    private int nextId = 1;

    @Override
    public Appointment save(Appointment appointment) {
        appointment.setId(nextId++);
        byNumber.put(appointment.getAppointmentNumber(), appointment);
        return appointment;
    }

    @Override
    public Optional<Appointment> findByNumber(String appointmentNumber) {
        return Optional.ofNullable(byNumber.get(appointmentNumber));
    }

    @Override
    public List<Appointment> findAll() {
        return new ArrayList<>(byNumber.values());
    }

    @Override
    public void update(Appointment appointment) {
        byNumber.put(appointment.getAppointmentNumber(), appointment);
    }

    @Override
    public boolean existsForDentistAtSlot(int dentistId, LocalDate date, LocalTime time) {
        return byNumber.values().stream().anyMatch(a ->
            a.getDentist().getDentistId() == dentistId
            && a.getAppointmentDate().equals(date)
            && a.getAppointmentTime().equals(time)
            && a.getStatus() != AppointmentStatus.CANCELLED
        );
    }
}
