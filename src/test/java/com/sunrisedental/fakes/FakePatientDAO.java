package com.sunrisedental.fakes;

import com.sunrisedental.dao.IPatientDAO;
import com.sunrisedental.domain.Patient;

import java.util.*;

public class FakePatientDAO implements IPatientDAO {

    private final Map<Integer, Patient> byId = new LinkedHashMap<>();
    private int nextId = 1;

    @Override
    public Patient save(Patient patient) {
        Patient stored = new Patient(nextId++, patient.getName(), patient.getAddress(), patient.getContactNumber());
        byId.put(stored.getPatientId(), stored);
        return stored;
    }

    @Override
    public Optional<Patient> findById(int patientId) {
        return Optional.ofNullable(byId.get(patientId));
    }

    @Override
    public List<Patient> findAll() {
        return new ArrayList<>(byId.values());
    }
}
