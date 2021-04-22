package io.github.willqi.pizzamc.claims.api.claims.dao;

import io.github.willqi.pizzamc.claims.api.claims.ChunkCoordinates;
import io.github.willqi.pizzamc.claims.api.claims.Claim;
import io.github.willqi.pizzamc.claims.api.exceptions.DaoException;

import java.util.Optional;
import java.util.UUID;

public interface ClaimsDao {

    Optional<Claim> getClaimByLocation(ChunkCoordinates location) throws DaoException;
    int getClaimCountOfUuid(UUID uuid) throws DaoException;

    void insert(Claim claim) throws DaoException;
    void update(Claim claim) throws DaoException;
    void delete(Claim claim) throws DaoException;

}
