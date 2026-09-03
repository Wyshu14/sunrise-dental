package com.sunrisedental.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TDD note: this class was written before PasswordUtil's verify() method
 * was finalised - the "wrong password fails" and "same password, two
 * salts, two different hashes" cases below were written first (red),
 * then PasswordUtil was written/adjusted until they passed (green), then
 * constant-time comparison was added as a refactor (refactor) without
 * changing any test.
 */
class PasswordUtilTest {

    @Test
    void hashingIsDeterministicForTheSameSalt() {
        String salt = PasswordUtil.generateSalt();
        String hash1 = PasswordUtil.hash("Admin@123", salt);
        String hash2 = PasswordUtil.hash("Admin@123", salt);
        assertEquals(hash1, hash2, "Hashing the same password with the same salt must always produce the same hash");
    }

    @Test
    void sameSaltDifferentPasswordsProduceDifferentHashes() {
        String salt = PasswordUtil.generateSalt();
        String hash1 = PasswordUtil.hash("Admin@123", salt);
        String hash2 = PasswordUtil.hash("Admin@124", salt);
        assertNotEquals(hash1, hash2);
    }

    @Test
    void twoUsersWithTheSamePasswordGetDifferentHashesBecauseSaltsDiffer() {
        String saltA = PasswordUtil.generateSalt();
        String saltB = PasswordUtil.generateSalt();
        String hashA = PasswordUtil.hash("Reception@123", saltA);
        String hashB = PasswordUtil.hash("Reception@123", saltB);
        assertNotEquals(saltA, saltB, "generateSalt() must not repeat");
        assertNotEquals(hashA, hashB, "identical passwords must not produce identical stored hashes");
    }

    @Test
    void verifyReturnsTrueForCorrectPassword() {
        String salt = PasswordUtil.generateSalt();
        String hash = PasswordUtil.hash("Admin@123", salt);
        assertTrue(PasswordUtil.verify("Admin@123", salt, hash));
    }

    @Test
    void verifyReturnsFalseForWrongPassword() {
        String salt = PasswordUtil.generateSalt();
        String hash = PasswordUtil.hash("Admin@123", salt);
        assertFalse(PasswordUtil.verify("wrong-password", salt, hash));
    }
}
