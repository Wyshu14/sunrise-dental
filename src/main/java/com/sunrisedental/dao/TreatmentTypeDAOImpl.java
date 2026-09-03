package com.sunrisedental.dao;

import com.sunrisedental.domain.TreatmentType;
import com.sunrisedental.patterns.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TreatmentTypeDAOImpl implements ITreatmentTypeDAO {

    @Override
    public Optional<TreatmentType> findById(int treatmentId) {
        String sql = "SELECT treatment_id, treatment_name, base_cost FROM treatment_types WHERE treatment_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, treatmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to look up treatment type " + treatmentId, e);
        }
        return Optional.empty();
    }

    @Override
    public List<TreatmentType> findAll() {
        List<TreatmentType> types = new ArrayList<>();
        String sql = "SELECT treatment_id, treatment_name, base_cost FROM treatment_types ORDER BY treatment_id";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) types.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to list treatment types", e);
        }
        return types;
    }

    private TreatmentType mapRow(ResultSet rs) throws SQLException {
        return new TreatmentType(rs.getInt("treatment_id"), rs.getString("treatment_name"), rs.getDouble("base_cost"));
    }
}
