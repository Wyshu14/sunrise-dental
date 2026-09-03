package com.sunrisedental.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Password hashing utility.
 *
 * Design note (documented in the report as an explicit assumption): a
 * production system should use a dedicated password-hashing library such
 * as BCrypt or Argon2. This project deliberately uses SHA-256 with a
 * per-user random salt instead, purely so the whole codebase compiles and
 * runs with zero external dependencies in constrained environments; the
 * security *principle* demonstrated - never store plaintext passwords,
 * always salt before hashing - is the same one BCrypt/Argon2 apply.
 * Swapping this class for a BCrypt-based one would not require any other
 * class to change, because every caller only depends on this class's
 * public methods (encapsulation).
 */
public final class PasswordUtil {

    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordUtil() { }

    public static String generateSalt() {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    public static String hash(String plainPassword, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(Base64.getDecoder().decode(salt));
            byte[] hashed = digest.digest(plainPassword.getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(hashed);
        } catch (NoSuchAlgorithmException | java.io.UnsupportedEncodingException e) {
            throw new IllegalStateException("Password hashing algorithm unavailable", e);
        }
    }

    public static boolean verify(String plainPassword, String salt, String expectedHash) {
        String actualHash = hash(plainPassword, salt);
        return constantTimeEquals(actualHash, expectedHash);
    }

    /** Avoids leaking timing information about how much of the hash matched. */
    private static boolean constantTimeEquals(String a, String b) {
        if (a.length() != b.length()) return false;
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }
}
