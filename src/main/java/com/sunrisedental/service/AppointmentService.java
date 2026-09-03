package com.sunrisedental.service;

import com.sunrisedental.dao.*;
import com.sunrisedental.domain.*;
import com.sunrisedental.patterns.AppointmentBuilder;
import com.sunrisedental.patterns.NotificationCenter;
import com.sunrisedental.util.AppointmentNumberGenerator;
import com.sunrisedental.util.InputValidator;
import com.sunrisedental.util.ServiceResult;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * FACADE PATTERN.
 *
 * Why: registering an appointment actually touches five different classes
 * (InputValidator, PatientDAO, DentistDAO, TreatmentTypeDAO, AppointmentDAO,
 * NotificationCenter, AppointmentNumberGenerator). Every caller - the REST
 * handler and, indirectly, the console client - should only need to know
 * about ONE simple method, registerAppointment(request). This class is
 * that single, simplified entry point: it hides the subsystem's internal
 * complexity and is exactly the object the "Register New Appointment" and
 * "Calculate and Print Bill" sequence diagrams in Task A call
 * "AppointmentService".
 */
public class AppointmentService {

    private final IPatientDAO patientDAO;
    private final IDentistDAO dentistDAO;
    private final ITreatmentTypeDAO treatmentTypeDAO;
    private final IAppointmentDAO appointmentDAO;
    private final NotificationCenter notificationCenter;

    public AppointmentService(IPatientDAO patientDAO, IDentistDAO dentistDAO, ITreatmentTypeDAO treatmentTypeDAO,
                               IAppointmentDAO appointmentDAO, NotificationCenter notificationCenter) {
        this.patientDAO = patientDAO;
        this.dentistDAO = dentistDAO;
        this.treatmentTypeDAO = treatmentTypeDAO;
        this.appointmentDAO = appointmentDAO;
        this.notificationCenter = notificationCenter;
    }

    /** Realises the "Register New Appointment" «include» "Validate Appointment Data" flow (Task A Figure 4). */
    public ServiceResult<Appointment> registerAppointment(AppointmentRegistrationRequest req) {
        List<String> errors = validate(req);
        if (!errors.isEmpty()) {
            return ServiceResult.fail(errors);
        }

        Dentist dentist = dentistDAO.findById(req.dentistId)
            .orElseThrow(() -> new IllegalArgumentException("Unknown dentist id " + req.dentistId));
        TreatmentType treatment = treatmentTypeDAO.findById(req.treatmentId)
            .orElseThrow(() -> new IllegalArgumentException("Unknown treatment type id " + req.treatmentId));

        LocalDate date = LocalDate.parse(req.date);
        LocalTime time = LocalTime.parse(req.time);

        if (appointmentDAO.existsForDentistAtSlot(req.dentistId, date, time)) {
            return ServiceResult.fail(List.of(
                "Dr. " + dentist.getName() + " already has an appointment at " + time + " on " + date + ". Choose a different slot."
            ));
        }

        Patient patient = patientDAO.save(new Patient(0, req.patientName.trim(), req.address.trim(), req.contactNumber.trim()));

        Appointment appointment = new AppointmentBuilder()
            .withAppointmentNumber(AppointmentNumberGenerator.next())
            .withPatient(patient)
            .withDentist(dentist)
            .withTreatmentType(treatment)
            .onDate(date)
            .atTime(time)
            .withStatus(AppointmentStatus.BOOKED)
            .build();

        Appointment saved = appointmentDAO.save(appointment);
        notificationCenter.notifyRegistered(saved);
        return ServiceResult.ok(saved);
    }

    /** «include»d by Register New Appointment, Display Appointment Details and Calculate & Print Bill (Task A Figure 1). */
    private List<String> validate(AppointmentRegistrationRequest req) {
        List<String> errors = new ArrayList<>();
        errors.addAll(InputValidator.validatePatientName(req.patientName));
        errors.addAll(InputValidator.validateAddress(req.address));
        errors.addAll(InputValidator.validateContactNumber(req.contactNumber));
        errors.addAll(InputValidator.validateAppointmentDate(req.date));
        errors.addAll(InputValidator.validateAppointmentTime(req.time));

        if (dentistDAO.findById(req.dentistId).isEmpty()) {
            errors.add("Selected dentist does not exist.");
        }
        if (treatmentTypeDAO.findById(req.treatmentId).isEmpty()) {
            errors.add("Selected treatment type does not exist.");
        }
        return errors;
    }

    public ServiceResult<Appointment> searchByNumber(String appointmentNumber) {
        if (appointmentNumber == null || appointmentNumber.isBlank()) {
            return ServiceResult.fail(List.of("Appointment number is required."));
        }
        Optional<Appointment> found = appointmentDAO.findByNumber(appointmentNumber.trim());
        return found.map(ServiceResult::ok)
            .orElseGet(() -> ServiceResult.fail(List.of("No appointment found with number " + appointmentNumber)));
    }

    public ServiceResult<Appointment> reschedule(String appointmentNumber, String newDate, String newTime) {
        List<String> errors = new ArrayList<>();
        errors.addAll(InputValidator.validateAppointmentDate(newDate));
        errors.addAll(InputValidator.validateAppointmentTime(newTime));
        if (!errors.isEmpty()) return ServiceResult.fail(errors);

        ServiceResult<Appointment> existing = searchByNumber(appointmentNumber);
        if (!existing.isSuccess()) return existing;

        Appointment appointment = existing.getData();
        appointment.reschedule(LocalDate.parse(newDate), LocalTime.parse(newTime));
        appointmentDAO.update(appointment);
        return ServiceResult.ok(appointment);
    }

    public ServiceResult<Appointment> cancel(String appointmentNumber) {
        ServiceResult<Appointment> existing = searchByNumber(appointmentNumber);
        if (!existing.isSuccess()) return existing;

        Appointment appointment = existing.getData();
        appointment.updateStatus(AppointmentStatus.CANCELLED);
        appointmentDAO.update(appointment);
        notificationCenter.notifyCancelled(appointment);
        return ServiceResult.ok(appointment);
    }

    public List<Appointment> findAll() {
        return appointmentDAO.findAll();
    }
}
