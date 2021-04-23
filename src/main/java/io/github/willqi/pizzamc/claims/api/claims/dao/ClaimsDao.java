package io.github.willqi.pizzamc.claims.api.claims.dao;

import io.github.willqi.pizzamc.claims.api.claims.ChunkCoordinates;
import io.github.willqi.pizzamc.claims.api.claims.Claim;
import io.github.willqi.pizzamc.claims.api.exceptions.DaoException;

import java.util.Optional;
import java.util.UUID;

/**
 * Communicator between the ClaimManager and the database for claims
 */
public interface ClaimsDao {

    /**
     * Retrieve a claim if one exists in the database
     * @param location
     * @return an optional with the claim if present
     * @throws DaoException
     */
    Optional<Claim> getClaimByLocation(ChunkCoordinates location) throws DaoException;
    int getClaimCountOfUuid(UUID uuid) throws DaoException;

    void insert(Claim claim) throws DaoException;
    void update(Claim claim) throws DaoException;
    void delete(Claim claim) throws DaoException;

}
