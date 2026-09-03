package com.sunrisedental.fakes;

import com.sunrisedental.dao.ITreatmentTypeDAO;
import com.sunrisedental.domain.TreatmentType;

import java.util.*;

public class FakeTreatmentTypeDAO implements ITreatmentTypeDAO {

    private final Map<Integer, TreatmentType> byId = new LinkedHashMap<>();

    public FakeTreatmentTypeDAO withSeedData() {
        byId.put(1, new TreatmentType(1, "Dental Check-up", 1000.00));
        byId.put(2, new TreatmentType(2, "Root Canal Treatment", 15000.00));
        return this;
    }

    @Override
    public Optional<TreatmentType> findById(int treatmentId) {
        return Optional.ofNullable(byId.get(treatmentId));
    }

    @Override
    public List<TreatmentType> findAll() {
        return new ArrayList<>(byId.values());
    }
}
