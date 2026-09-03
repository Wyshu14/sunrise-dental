package com.sunrisedental.domain;

import java.time.LocalDate;

/**
 * Generated from an Appointment (composition in the Task A class diagram:
 * a Bill cannot exist without the Appointment that produced it).
 */
public class Bill {

    private int billId;
    private String appointmentNumber;
    private double consultationFee;
    private double treatmentCost;
    private double discountAmount;
    private double totalAmount;
    private LocalDate generatedDate;

    public Bill(int billId, String appointmentNumber, double consultationFee, double treatmentCost,
                double discountAmount, LocalDate generatedDate) {
        this.billId = billId;
        this.appointmentNumber = appointmentNumber;
        this.consultationFee = consultationFee;
        this.treatmentCost = treatmentCost;
        this.discountAmount = discountAmount;
        this.generatedDate = generatedDate;
        this.totalAmount = calculateTotal();
    }

    /** consultation fee + treatment cost, minus any discount/insurance adjustment (the «extend» use case in Task A). */
    public double calculateTotal() {
        double total = consultationFee + treatmentCost - discountAmount;
        this.totalAmount = Math.max(total, 0.0);
        return this.totalAmount;
    }

    public String printReceipt() {
        StringBuilder sb = new StringBuilder();
        sb.append("========================================\n");
        sb.append("        SUNRISE DENTAL CLINIC\n");
        sb.append("            PATIENT RECEIPT\n");
        sb.append("========================================\n");
        sb.append("Bill No        : ").append(billId).append("\n");
        sb.append("Appointment No : ").append(appointmentNumber).append("\n");
        sb.append("Date           : ").append(generatedDate).append("\n");
        sb.append("----------------------------------------\n");
        sb.append(String.format("Consultation Fee : Rs. %.2f%n", consultationFee));
        sb.append(String.format("Treatment Cost   : Rs. %.2f%n", treatmentCost));
        if (discountAmount > 0) {
            sb.append(String.format("Discount/Cover   : -Rs. %.2f%n", discountAmount));
        }
        sb.append("----------------------------------------\n");
        sb.append(String.format("TOTAL AMOUNT     : Rs. %.2f%n", totalAmount));
        sb.append("========================================\n");
        sb.append("        Thank you for visiting us!\n");
        sb.append("========================================\n");
        return sb.toString();
    }

    public int getBillId() { return billId; }
    public String getAppointmentNumber() { return appointmentNumber; }
    public double getConsultationFee() { return consultationFee; }
    public double getTreatmentCost() { return treatmentCost; }
    public double getDiscountAmount() { return discountAmount; }
    public double getTotalAmount() { return totalAmount; }
    public LocalDate getGeneratedDate() { return generatedDate; }
}
