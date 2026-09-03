package com.sunrisedental.service;

import com.sunrisedental.domain.User;
import com.sunrisedental.fakes.FakeUserDAO;
import com.sunrisedental.patterns.UserFactory;
import com.sunrisedental.util.ServiceResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Realises the Login sequence diagram (Task A Figure 3) as an automated test. */
class AuthenticationServiceTest {

    private FakeUserDAO userDAO;
    private AuthenticationService authService;

    @BeforeEach
    void setUp() {
        userDAO = new FakeUserDAO();
        authService = new AuthenticationService(userDAO);
        userDAO.save(UserFactory.createUser(1, "reception1", "Reception@123", "Nadeesha Perera", "RECEPTIONIST"));
    }

    @Test
    void correctCredentialsLogIn() {
        ServiceResult<User> result = authService.login("reception1", "Reception@123");
        assertTrue(result.isSuccess());
        assertEquals("RECEPTIONIST", result.getData().getRole());
    }

    @Test
    void wrongPasswordIsRejectedWithGenericMessage() {
        ServiceResult<User> result = authService.login("reception1", "wrong-password");
        assertFalse(result.isSuccess());
        assertEquals("Invalid username or password.", result.getErrors().get(0));
    }

    @Test
    void unknownUsernameGivesTheSameGenericMessageAsWrongPassword() {
        // Regression test for the security decision documented in Task A Section 4.1:
        // an attacker must not be able to tell "unknown user" apart from "wrong password".
        ServiceResult<User> unknownUserResult = authService.login("nobody", "whatever");
        ServiceResult<User> wrongPasswordResult = authService.login("reception1", "wrong-password");
        assertEquals(unknownUserResult.getErrors(), wrongPasswordResult.getErrors());
    }

    @Test
    void blankUsernameIsRejectedBeforeHittingTheDatabase() {
        ServiceResult<User> result = authService.login("", "Reception@123");
        assertFalse(result.isSuccess());
        assertTrue(result.getErrors().contains("Username is required."));
    }

    @Test
    void blankPasswordIsRejected() {
        ServiceResult<User> result = authService.login("reception1", "");
        assertFalse(result.isSuccess());
        assertTrue(result.getErrors().contains("Password is required."));
    }
}
