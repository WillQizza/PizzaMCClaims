package io.github.willqi.pizzamc.claims.api.homes.database.impl;

import io.github.willqi.pizzamc.claims.api.homes.Home;
import io.github.willqi.pizzamc.claims.api.homes.database.HomesDao;

import java.util.Set;
import java.util.UUID;

public class HikariHomesDao implements HomesDao {

    @Override
    public Set<Home> getHomesByOwner(UUID uuid) {
        return null;
    }

    @Override
    public void insert(Home home) {

    }

    @Override
    public void update(Home home) {

    }

    @Override
    public void delete(Home home) {

    }

}
