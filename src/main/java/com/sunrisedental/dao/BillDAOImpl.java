package com.sunrisedental.dao;

import com.sunrisedental.domain.Bill;
import com.sunrisedental.patterns.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BillDAOImpl implements IBillDAO {

    @Override
    public Bill save(Bill bill) {
        String sql = "INSERT INTO bills (appointment_number, consultation_fee, treatment_cost, discount_amount, total_amount, generated_date) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, bill.getAppointmentNumber());
            ps.setDouble(2, bill.getConsultationFee());
            ps.setDouble(3, bill.getTreatmentCost());
            ps.setDouble(4, bill.getDiscountAmount());
            ps.setDouble(5, bill.getTotalAmount());
            ps.setDate(6, Date.valueOf(bill.getGeneratedDate()));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return new Bill(keys.getInt(1), bill.getAppointmentNumber(), bill.getConsultationFee(),
                        bill.getTreatmentCost(), bill.getDiscountAmount(), bill.getGeneratedDate());
                }
            }
            return bill;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save bill for appointment " + bill.getAppointmentNumber(), e);
        }
    }

    @Override
    public Optional<Bill> findByAppointmentNumber(String appointmentNumber) {
        String sql = "SELECT * FROM bills WHERE appointment_number = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, appointmentNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Bill(
                        rs.getInt("bill_id"), rs.getString("appointment_number"),
                        rs.getDouble("consultation_fee"), rs.getDouble("treatment_cost"),
                        rs.getDouble("discount_amount"), rs.getDate("generated_date").toLocalDate()
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to look up bill for appointment " + appointmentNumber, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Bill> findAll() {
        List<Bill> bills = new ArrayList<>();
        String sql = "SELECT * FROM bills ORDER BY generated_date";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                bills.add(new Bill(
                    rs.getInt("bill_id"), rs.getString("appointment_number"),
                    rs.getDouble("consultation_fee"), rs.getDouble("treatment_cost"),
                    rs.getDouble("discount_amount"), rs.getDate("generated_date").toLocalDate()
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to list bills", e);
        }
        return bills;
    }
}
