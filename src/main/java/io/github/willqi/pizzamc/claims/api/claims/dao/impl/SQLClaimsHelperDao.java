package io.github.willqi.pizzamc.claims.api.claims.dao.impl;

import com.zaxxer.hikari.HikariDataSource;
import io.github.willqi.pizzamc.claims.api.claims.ChunkCoordinates;
import io.github.willqi.pizzamc.claims.api.claims.ClaimHelper;
import io.github.willqi.pizzamc.claims.api.claims.dao.ClaimsHelperDao;
import io.github.willqi.pizzamc.claims.api.exceptions.DaoException;

import java.util.Set;

public class SQLClaimsHelperDao implements ClaimsHelperDao {

    private final HikariDataSource source;

    public SQLClaimsHelperDao(HikariDataSource source) {
        this.source = source;

    }

    @Override
    public Set<ClaimHelper> getClaimHelpersByLocation(ChunkCoordinates location) throws DaoException {
        return null;
    }

    @Override
    public void delete(ChunkCoordinates claimCoords, ClaimHelper helper) throws DaoException {

    }

    @Override
    public void update(ChunkCoordinates claimCoords, ClaimHelper helper) throws DaoException {

    }

    @Override
    public void insert(ChunkCoordinates claimCoords, ClaimHelper helper) throws DaoException {

    }

}
