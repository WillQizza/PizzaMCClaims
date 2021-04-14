package io.github.willqi.pizzamc.claims.api.claims.database.impl;

import com.zaxxer.hikari.pool.HikariPool;
import io.github.willqi.pizzamc.claims.api.claims.ChunkCoordinates;
import io.github.willqi.pizzamc.claims.api.claims.Claim;
import io.github.willqi.pizzamc.claims.api.claims.database.ClaimsDao;

import java.util.Optional;

public class HikariClaimsDao implements ClaimsDao {

    private final HikariPool pool;

    public HikariClaimsDao(HikariPool pool) {
        this.pool = pool;

    }

    @Override
    public Optional<Claim> getClaimByLocation(ChunkCoordinates location) {
        return null;
    }

    @Override
    public void delete(Claim claim) {

    }

    @Override
    public void update(Claim claim) {

    }

    @Override
    public void insert(Claim claim) {

    }

}
