package com.sunrisedental.dao;

import com.sunrisedental.domain.Patient;
import java.util.List;
import java.util.Optional;

public interface IPatientDAO {
    Patient save(Patient patient);
    Optional<Patient> findById(int patientId);
    List<Patient> findAll();
}
