package com.sunrisedental.dao;

import com.sunrisedental.domain.TreatmentType;
import java.util.List;
import java.util.Optional;

public interface ITreatmentTypeDAO {
    Optional<TreatmentType> findById(int treatmentId);
    List<TreatmentType> findAll();
}
