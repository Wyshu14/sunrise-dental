package com.sunrisedental.patterns;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * SINGLETON PATTERN.
 *
 * Why: the application should share one configured connection (in a real
 * deployment, one pooled DataSource) to the MySQL server rather than every
 * DAO opening its own connection - opening a fresh TCP/auth handshake per
 * query would be wasteful and would make connection-limit exhaustion easy.
 * The private constructor and static getInstance() enforce that exactly
 * one instance of this class can ever exist in the JVM.
 *
 * Thread-safety: getInstance() is synchronized, and double-checked locking
 * is used so the synchronization cost is only paid on the very first call.
 */
public final class DatabaseConnection {

    private static volatile DatabaseConnection instance;

    private final String url;
    private final String user;
    private final String password;

    private DatabaseConnection(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
        try {
            // Explicit driver load kept for clarity/older-JDBC compatibility;
            // JDBC 4+ would auto-register it via META-INF/services.
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                "MySQL JDBC driver not found on the classpath. Run this project with Maven " +
                "(mvn compile exec:java) so the mysql-connector-j dependency is resolved.", e);
        }
    }

    public static DatabaseConnection getInstance() {
        if (instance == null) {
            synchronized (DatabaseConnection.class) {
                if (instance == null) {
                    String url = System.getProperty("db.url", "jdbc:mysql://localhost:3306/sunrise_dental?useSSL=false&serverTimezone=UTC");
                    String user = System.getProperty("db.user", "root");
                    String password = System.getProperty("db.password", "");
                    instance = new DatabaseConnection(url, user, password);
                }
            }
        }
        return instance;
    }

    /** Returns a fresh live Connection each call; callers are responsible for closing it (try-with-resources). */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
}
