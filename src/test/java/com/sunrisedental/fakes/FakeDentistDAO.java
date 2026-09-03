package com.sunrisedental.fakes;

import com.sunrisedental.dao.IDentistDAO;
import com.sunrisedental.domain.Dentist;

import java.util.*;

public class FakeDentistDAO implements IDentistDAO {

    private final Map<Integer, Dentist> byId = new LinkedHashMap<>();

    public FakeDentistDAO withSeedData() {
        byId.put(1, new Dentist(1, "Dr. S. Fernando", "General Dentistry"));
        byId.put(2, new Dentist(2, "Dr. R. Jayawardena", "Orthodontics"));
        return this;
    }

    @Override
    public Optional<Dentist> findById(int dentistId) {
        return Optional.ofNullable(byId.get(dentistId));
    }

    @Override
    public List<Dentist> findAll() {
        return new ArrayList<>(byId.values());
    }
}
