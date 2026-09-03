package com.sunrisedental.dao;

import com.sunrisedental.domain.*;
import com.sunrisedental.patterns.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** DAO PATTERN - concrete MySQL implementation (AppointmentDAOImpl in Task A Figure 2). */
public class AppointmentDAOImpl implements IAppointmentDAO {

    private final IPatientDAO patientDAO = new PatientDAOImpl();
    private final IDentistDAO dentistDAO = new DentistDAOImpl();
    private final ITreatmentTypeDAO treatmentTypeDAO = new TreatmentTypeDAOImpl();

    @Override
    public Appointment save(Appointment appointment) {
        String sql = "INSERT INTO appointments " +
                     "(appointment_number, patient_id, dentist_id, treatment_id, appointment_date, appointment_time, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, appointment.getAppointmentNumber());
            ps.setInt(2, appointment.getPatient().getPatientId());
            ps.setInt(3, appointment.getDentist().getDentistId());
            ps.setInt(4, appointment.getTreatmentType().getTreatmentId());
            ps.setDate(5, Date.valueOf(appointment.getAppointmentDate()));
            ps.setTime(6, Time.valueOf(appointment.getAppointmentTime()));
            ps.setString(7, appointment.getStatus().name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    appointment.setId(keys.getInt(1));
                }
            }
            return appointment;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save appointment " + appointment.getAppointmentNumber(), e);
        }
    }

    @Override
    public Optional<Appointment> findByNumber(String appointmentNumber) {
        String sql = "SELECT * FROM appointments WHERE appointment_number = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, appointmentNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to look up appointment " + appointmentNumber, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Appointment> findAll() {
        List<Appointment> appointments = new ArrayList<>();
        String sql = "SELECT * FROM appointments ORDER BY appointment_date, appointment_time";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) appointments.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to list appointments", e);
        }
        return appointments;
    }

    @Override
    public void update(Appointment appointment) {
        String sql = "UPDATE appointments SET appointment_date = ?, appointment_time = ?, status = ? WHERE appointment_number = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(appointment.getAppointmentDate()));
            ps.setTime(2, Time.valueOf(appointment.getAppointmentTime()));
            ps.setString(3, appointment.getStatus().name());
            ps.setString(4, appointment.getAppointmentNumber());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update appointment " + appointment.getAppointmentNumber(), e);
        }
    }

    @Override
    public boolean existsForDentistAtSlot(int dentistId, LocalDate date, LocalTime time) {
        String sql = "SELECT COUNT(*) FROM appointments " +
                     "WHERE dentist_id = ? AND appointment_date = ? AND appointment_time = ? AND status <> 'CANCELLED'";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, dentistId);
            ps.setDate(2, Date.valueOf(date));
            ps.setTime(3, Time.valueOf(time));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check double-booking for dentist " + dentistId, e);
        }
        return false;
    }

    private Appointment mapRow(ResultSet rs) throws SQLException {
        Patient patient = patientDAO.findById(rs.getInt("patient_id"))
            .orElseThrow(() -> new IllegalStateException("Orphaned appointment: missing patient"));
        Dentist dentist = dentistDAO.findById(rs.getInt("dentist_id"))
            .orElseThrow(() -> new IllegalStateException("Orphaned appointment: missing dentist"));
        TreatmentType treatment = treatmentTypeDAO.findById(rs.getInt("treatment_id"))
            .orElseThrow(() -> new IllegalStateException("Orphaned appointment: missing treatment type"));

        return new Appointment(
            rs.getInt("id"),
            rs.getString("appointment_number"),
            patient,
            dentist,
            treatment,
            rs.getDate("appointment_date").toLocalDate(),
            rs.getTime("appointment_time").toLocalTime(),
            AppointmentStatus.valueOf(rs.getString("status"))
        );
    }
}
