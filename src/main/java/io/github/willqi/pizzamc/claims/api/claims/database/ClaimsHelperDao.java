package io.github.willqi.pizzamc.claims.api.claims.database;

import io.github.willqi.pizzamc.claims.api.claims.ChunkCoordinates;
import io.github.willqi.pizzamc.claims.api.claims.ClaimHelper;

import java.util.Optional;
import java.util.Set;

public interface ClaimsHelperDao {

    Set<ClaimHelper> getClaimHelpersByLocation(ChunkCoordinates location);
    Optional<ClaimHelper> getClaimHelperByLocation(ChunkCoordinates location);

    void delete(ChunkCoordinates claimCoords, ClaimHelper helper);
    void update(ChunkCoordinates claimCoords, ClaimHelper helper);
    void insert(ChunkCoordinates claimCoords, ClaimHelper helper);


}
