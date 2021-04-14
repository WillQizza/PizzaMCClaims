package io.github.willqi.pizzamc.claims.api.claims.database.impl;

import com.zaxxer.hikari.pool.HikariPool;
import io.github.willqi.pizzamc.claims.api.claims.ChunkCoordinates;
import io.github.willqi.pizzamc.claims.api.claims.ClaimHelper;
import io.github.willqi.pizzamc.claims.api.claims.database.ClaimsHelperDao;

import java.util.Optional;
import java.util.Set;

public class HikariClaimsHelperDao implements ClaimsHelperDao {

    private final HikariPool pool;

    public HikariClaimsHelperDao(HikariPool pool) {
        this.pool = pool;

    }

    @Override
    public Set<ClaimHelper> getClaimHelpersByLocation(ChunkCoordinates location) {
        return null;
    }

    @Override
    public Optional<ClaimHelper> getClaimHelperByLocation(ChunkCoordinates location) {
        return null;
    }

    @Override
    public void delete(ChunkCoordinates claimCoords, ClaimHelper helper) {

    }

    @Override
    public void update(ChunkCoordinates claimCoords, ClaimHelper helper) {

    }

    @Override
    public void insert(ChunkCoordinates claimCoords, ClaimHelper helper) {

    }

}
