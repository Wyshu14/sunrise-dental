package com.sunrisedental.dao;

import com.sunrisedental.domain.User;
import java.util.Optional;

/**
 * DAO PATTERN (interface half).
 * The service layer depends only on this interface, never on the concrete
 * MySQL implementation - so the persistence technology can be swapped
 * (e.g. for the in-memory fake used in unit tests, see test sources)
 * without changing a single line of business logic.
 */
public interface IUserDAO {
    Optional<User> findByUsername(String username);
    void save(User user);
}
