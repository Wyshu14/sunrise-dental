package com.sunrisedental.domain;

/**
 * Abstract base class for anyone who can log in to the system.
 * Matches the User class in the Task A class diagram: it is never
 * instantiated directly, only through its subclasses Receptionist
 * and Administrator (see the generalisation arrows in Figure 2).
 */
public abstract class User {

    protected int userId;
    protected String username;
    protected String passwordHash; // SHA-256 + per-user salt, see PasswordUtil
    protected String passwordSalt;
    protected String role; // "RECEPTIONIST" or "ADMINISTRATOR"

    protected User(int userId, String username, String passwordHash, String passwordSalt, String role) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.passwordSalt = passwordSalt;
        this.role = role;
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getPasswordSalt() {
        return passwordSalt;
    }

    public String getRole() {
        return role;
    }

    /**
     * Every user type must be able to describe what it is allowed to do,
     * used by the console client to build its menu after login.
     */
    public abstract String[] getMenuOptions();
}
