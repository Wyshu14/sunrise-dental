package com.sunrisedental.domain;

/** Matches the Patient class in the Task A class diagram. */
public class Patient {

    private int patientId;
    private String name;
    private String address;
    private String contactNumber;

    public Patient(int patientId, String name, String address, String contactNumber) {
        this.patientId = patientId;
        this.name = name;
        this.address = address;
        this.contactNumber = contactNumber;
    }

    public int getPatientId() { return patientId; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public String getContactNumber() { return contactNumber; }

    public String getContactDetails() {
        return name + " | " + address + " | " + contactNumber;
    }

    @Override
    public String toString() {
        return "Patient{id=" + patientId + ", name='" + name + "'}";
    }
}
