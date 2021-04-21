package io.github.willqi.pizzamc.claims.api.users.dao;

import io.github.willqi.pizzamc.claims.api.exceptions.DaoException;
import io.github.willqi.pizzamc.claims.api.users.User;

import java.util.Optional;
import java.util.UUID;

public interface UsersDao {

    Optional<User> getUserByName(String name) throws DaoException;
    Optional<User> getUserByUuid(UUID uuid) throws DaoException;

    void insert(User user) throws DaoException;
    void update(User user) throws DaoException;
    void delete(User user) throws DaoException;

}
