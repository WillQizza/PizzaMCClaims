package io.github.willqi.pizzamc.claims.api.claims.dao;

import io.github.willqi.pizzamc.claims.api.claims.ChunkCoordinates;
import io.github.willqi.pizzamc.claims.api.claims.ClaimHelper;
import io.github.willqi.pizzamc.claims.api.exceptions.DaoException;

import java.util.Set;

public interface ClaimsHelperDao {

    Set<ClaimHelper> getClaimHelpersByLocation(ChunkCoordinates location) throws DaoException;

    void insert(ChunkCoordinates claimCoords, ClaimHelper helper) throws DaoException;
    void update(ChunkCoordinates claimCoords, ClaimHelper helper) throws DaoException;
    void delete(ChunkCoordinates claimCoords, ClaimHelper helper) throws DaoException;


}
