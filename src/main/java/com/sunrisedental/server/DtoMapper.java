package com.sunrisedental.server;

import com.sunrisedental.domain.*;
import com.sunrisedental.util.Json;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Converts domain objects into plain Map/List structures that Json.write() can serialise. */
public final class DtoMapper {

    private DtoMapper() { }

    public static Map<String, Object> toJson(Appointment a) {
        return Json.obj(
            "appointmentNumber", a.getAppointmentNumber(),
            "patientName", a.getPatient().getName(),
            "patientAddress", a.getPatient().getAddress(),
            "patientContact", a.getPatient().getContactNumber(),
            "dentistName", a.getDentist().getName(),
            "treatmentType", a.getTreatmentType().getTreatmentName(),
            "date", a.getAppointmentDate().toString(),
            "time", a.getAppointmentTime().toString(),
            "status", a.getStatus().name()
        );
    }

    public static Map<String, Object> toJson(Bill b) {
        return Json.obj(
            "billId", b.getBillId(),
            "appointmentNumber", b.getAppointmentNumber(),
            "consultationFee", b.getConsultationFee(),
            "treatmentCost", b.getTreatmentCost(),
            "discountAmount", b.getDiscountAmount(),
            "totalAmount", b.getTotalAmount(),
            "generatedDate", b.getGeneratedDate().toString(),
            "receiptText", b.printReceipt()
        );
    }

    public static Map<String, Object> toJson(User u) {
        return Json.obj(
            "username", u.getUsername(),
            "role", u.getRole(),
            "menuOptions", List.of(u.getMenuOptions())
        );
    }

    public static Map<String, Object> toJson(Dentist d) {
        return Json.obj("dentistId", d.getDentistId(), "name", d.getName(), "specialization", d.getSpecialization());
    }

    public static Map<String, Object> toJson(TreatmentType t) {
        return Json.obj("treatmentId", t.getTreatmentId(), "treatmentName", t.getTreatmentName(), "baseCost", t.getCost());
    }

    public static List<Map<String, Object>> toJsonList(List<?> items) {
        return items.stream().map(item -> switch (item) {
            case Appointment a -> toJson(a);
            case Dentist d -> toJson(d);
            case TreatmentType t -> toJson(t);
            default -> throw new IllegalArgumentException("No JSON mapping for " + item.getClass());
        }).collect(Collectors.toList());
    }
}
