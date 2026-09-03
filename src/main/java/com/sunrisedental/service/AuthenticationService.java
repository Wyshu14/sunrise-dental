package com.sunrisedental.service;

import com.sunrisedental.dao.IUserDAO;
import com.sunrisedental.domain.User;
import com.sunrisedental.util.InputValidator;
import com.sunrisedental.util.PasswordUtil;
import com.sunrisedental.util.ServiceResult;

import java.util.List;
import java.util.Optional;

/**
 * Realises the Login sequence diagram (Task A, Figure 3).
 * Deliberately returns the SAME generic error for "unknown username" and
 * "wrong password" so a caller cannot enumerate valid usernames by
 * observing different error messages (an ethical/secure-coding decision
 * documented as an assumption in the report).
 */
public class AuthenticationService {

    private static final String GENERIC_LOGIN_ERROR = "Invalid username or password.";

    private final IUserDAO userDAO;

    public AuthenticationService(IUserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public ServiceResult<User> login(String username, String password) {
        List<String> inputErrors = InputValidator.validateLoginInput(username, password);
        if (!inputErrors.isEmpty()) {
            return ServiceResult.fail(inputErrors);
        }

        Optional<User> userOpt = userDAO.findByUsername(username.trim());
        if (userOpt.isEmpty()) {
            return ServiceResult.fail(List.of(GENERIC_LOGIN_ERROR));
        }

        User user = userOpt.get();
        boolean valid = PasswordUtil.verify(password, user.getPasswordSalt(), user.getPasswordHash());
        if (!valid) {
            return ServiceResult.fail(List.of(GENERIC_LOGIN_ERROR));
        }

        return ServiceResult.ok(user);
    }
}
