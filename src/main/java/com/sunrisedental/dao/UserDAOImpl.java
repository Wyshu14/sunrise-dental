package com.sunrisedental.dao;

import com.sunrisedental.domain.Administrator;
import com.sunrisedental.domain.Receptionist;
import com.sunrisedental.domain.User;
import com.sunrisedental.patterns.DatabaseConnection;

import java.sql.*;
import java.util.Optional;

/** MySQL-backed implementation of IUserDAO (DAO pattern - concrete half). */
public class UserDAOImpl implements IUserDAO {

    @Override
    public Optional<User> findByUsername(String username) {
        String sql = "SELECT user_id, username, password_hash, password_salt, role, staff_name " +
                     "FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to look up user '" + username + "'", e);
        }
        return Optional.empty();
    }

    @Override
    public void save(User user) {
        String sql = "INSERT INTO users (username, password_hash, password_salt, role, staff_name) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getPasswordSalt());
            ps.setString(4, user.getRole());
            ps.setString(5, (user instanceof Receptionist r) ? r.getStaffName() : "");
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save user '" + user.getUsername() + "'", e);
        }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        int id = rs.getInt("user_id");
        String username = rs.getString("username");
        String hash = rs.getString("password_hash");
        String salt = rs.getString("password_salt");
        String role = rs.getString("role");
        String staffName = rs.getString("staff_name");

        if ("ADMINISTRATOR".equals(role)) {
            return new Administrator(id, username, hash, salt, staffName);
        }
        return new Receptionist(id, username, hash, salt, staffName);
    }
}
