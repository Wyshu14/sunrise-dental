package com.sunrisedental.fakes;

import com.sunrisedental.dao.IUserDAO;
import com.sunrisedental.domain.User;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * In-memory stand-in for UserDAOImpl, used only by the automated test
 * suite (Task C). Implementing IAppointmentDAO's sibling interfaces
 * against a HashMap instead of a real MySQL database is the standard
 * technique for isolated unit testing: AppointmentService/AuthenticationService's
 * logic is exercised without needing a live database connection, and
 * tests run in milliseconds instead of seconds.
 */
public class FakeUserDAO implements IUserDAO {

    private final Map<String, User> byUsername = new LinkedHashMap<>();

    @Override
    public Optional<User> findByUsername(String username) {
        return Optional.ofNullable(byUsername.get(username));
    }

    @Override
    public void save(User user) {
        byUsername.put(user.getUsername(), user);
    }
}
