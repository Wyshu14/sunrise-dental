package com.sunrisedental.patterns;

import com.sunrisedental.domain.Administrator;
import com.sunrisedental.domain.Receptionist;
import com.sunrisedental.domain.User;
import com.sunrisedental.util.PasswordUtil;

/**
 * FACTORY PATTERN.
 *
 * Why: creating a User involves a small piece of logic (deciding which
 * concrete subclass to instantiate based on a role string, and generating
 * the salted password hash) that callers should not have to repeat every
 * time a new account is created. Centralising it here means
 * Manage Staff Accounts (Administrator only) and the initial database
 * seed both create users the exact same, correct way.
 */
public final class UserFactory {

    private UserFactory() { }

    public static User createUser(int userId, String username, String plainPassword, String staffName, String role) {
        String salt = PasswordUtil.generateSalt();
        String hash = PasswordUtil.hash(plainPassword, salt);

        return switch (role.toUpperCase()) {
            case "ADMINISTRATOR" -> new Administrator(userId, username, hash, salt, staffName);
            case "RECEPTIONIST" -> new Receptionist(userId, username, hash, salt, staffName);
            default -> throw new IllegalArgumentException("Unknown role: " + role);
        };
    }
}
