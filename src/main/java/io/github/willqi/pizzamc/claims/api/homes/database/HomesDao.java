package io.github.willqi.pizzamc.claims.api.homes.database;

import io.github.willqi.pizzamc.claims.api.homes.Home;

import java.util.Set;
import java.util.UUID;

public interface HomesDao {

    Set<Home> getHomesByOwner(UUID uuid);

    void insert(Home home);
    void update(Home home);
    void delete(Home home);

}
