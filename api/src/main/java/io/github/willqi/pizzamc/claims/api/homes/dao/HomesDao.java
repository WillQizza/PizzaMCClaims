package io.github.willqi.pizzamc.claims.api.homes.dao;

import io.github.willqi.pizzamc.claims.api.exceptions.DaoException;
import io.github.willqi.pizzamc.claims.api.homes.Home;

import java.util.Set;
import java.util.UUID;

public interface HomesDao {

    Set<Home> getHomesByOwner(UUID uuid) throws DaoException;

    void insert(Home home) throws DaoException;
    void update(Home home) throws DaoException;
    void delete(Home home) throws DaoException;

}
