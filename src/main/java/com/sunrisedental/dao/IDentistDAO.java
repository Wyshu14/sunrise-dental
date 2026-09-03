package com.sunrisedental.dao;

import com.sunrisedental.domain.Dentist;
import java.util.List;
import java.util.Optional;

public interface IDentistDAO {
    Optional<Dentist> findById(int dentistId);
    List<Dentist> findAll();
}
