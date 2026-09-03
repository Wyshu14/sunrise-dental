package com.sunrisedental.domain;

/** Matches the Dentist class in the Task A class diagram. */
public class Dentist {

    private int dentistId;
    private String name;
    private String specialization;

    public Dentist(int dentistId, String name, String specialization) {
        this.dentistId = dentistId;
        this.name = name;
        this.specialization = specialization;
    }

    public int getDentistId() { return dentistId; }
    public String getName() { return name; }
    public String getSpecialization() { return specialization; }

    @Override
    public String toString() {
        return "Dr. " + name + " (" + specialization + ")";
    }
}
