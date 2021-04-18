package io.github.willqi.pizzamc.claims.api.claims.dao.impl;

import com.zaxxer.hikari.HikariDataSource;
import io.github.willqi.pizzamc.claims.api.claims.ChunkCoordinates;
import io.github.willqi.pizzamc.claims.api.claims.Claim;
import io.github.willqi.pizzamc.claims.api.claims.dao.ClaimsDao;
import io.github.willqi.pizzamc.claims.api.exceptions.DaoException;

import java.util.Optional;

public class SQLClaimsDao implements ClaimsDao {

    private final HikariDataSource source;

    public SQLClaimsDao(HikariDataSource source) {
        this.source = source;
    }

    @Override
    public Optional<Claim> getClaimByLocation(ChunkCoordinates location) throws DaoException {
        return null;
    }

    @Override
    public void delete(Claim claim) throws DaoException {

    }

    @Override
    public void update(Claim claim) throws DaoException {

    }

    @Override
    public void insert(Claim claim) throws DaoException {

    }

}
