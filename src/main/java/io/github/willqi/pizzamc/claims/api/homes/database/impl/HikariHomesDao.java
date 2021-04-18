package io.github.willqi.pizzamc.claims.api.homes.database.impl;

import io.github.willqi.pizzamc.claims.api.exceptions.DaoException;
import io.github.willqi.pizzamc.claims.api.homes.Home;
import io.github.willqi.pizzamc.claims.api.homes.database.HomesDao;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class HikariHomesDao implements HomesDao {

    public HikariHomesDao() {

    }

    @Override
    public Set<Home> getHomesByOwner(UUID uuid) throws DaoException {
        return new HashSet<>();
    }

    @Override
    public void insert(Home home) throws DaoException {

    }

    @Override
    public void update(Home home) throws DaoException {

    }

    @Override
    public void delete(Home home) throws DaoException {

    }

}
