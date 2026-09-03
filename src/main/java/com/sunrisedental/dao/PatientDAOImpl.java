package com.sunrisedental.dao;

import com.sunrisedental.domain.Patient;
import com.sunrisedental.patterns.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PatientDAOImpl implements IPatientDAO {

    @Override
    public Patient save(Patient patient) {
        String sql = "INSERT INTO patients (name, address, contact_number) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, patient.getName());
            ps.setString(2, patient.getAddress());
            ps.setString(3, patient.getContactNumber());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return new Patient(keys.getInt(1), patient.getName(), patient.getAddress(), patient.getContactNumber());
                }
            }
            return patient;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save patient '" + patient.getName() + "'", e);
        }
    }

    @Override
    public Optional<Patient> findById(int patientId) {
        String sql = "SELECT patient_id, name, address, contact_number FROM patients WHERE patient_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, patientId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to look up patient " + patientId, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Patient> findAll() {
        List<Patient> patients = new ArrayList<>();
        String sql = "SELECT patient_id, name, address, contact_number FROM patients ORDER BY patient_id";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                patients.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to list patients", e);
        }
        return patients;
    }

    private Patient mapRow(ResultSet rs) throws SQLException {
        return new Patient(rs.getInt("patient_id"), rs.getString("name"), rs.getString("address"), rs.getString("contact_number"));
    }
}
