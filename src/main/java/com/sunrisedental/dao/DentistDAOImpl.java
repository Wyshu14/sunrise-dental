package com.sunrisedental.dao;

import com.sunrisedental.domain.Dentist;
import com.sunrisedental.patterns.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DentistDAOImpl implements IDentistDAO {

    @Override
    public Optional<Dentist> findById(int dentistId) {
        String sql = "SELECT dentist_id, name, specialization FROM dentists WHERE dentist_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, dentistId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to look up dentist " + dentistId, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Dentist> findAll() {
        List<Dentist> dentists = new ArrayList<>();
        String sql = "SELECT dentist_id, name, specialization FROM dentists ORDER BY dentist_id";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) dentists.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to list dentists", e);
        }
        return dentists;
    }

    private Dentist mapRow(ResultSet rs) throws SQLException {
        return new Dentist(rs.getInt("dentist_id"), rs.getString("name"), rs.getString("specialization"));
    }
}
