package com.sunrisedental.domain;

/** Matches the TreatmentType class in the Task A class diagram. */
public class TreatmentType {

    private int treatmentId;
    private String treatmentName;
    private double baseCost;

    public TreatmentType(int treatmentId, String treatmentName, double baseCost) {
        this.treatmentId = treatmentId;
        this.treatmentName = treatmentName;
        this.baseCost = baseCost;
    }

    public int getTreatmentId() { return treatmentId; }
    public String getTreatmentName() { return treatmentName; }
    public double getCost() { return baseCost; }

    @Override
    public String toString() {
        return treatmentName + " (Rs. " + baseCost + ")";
    }
}
