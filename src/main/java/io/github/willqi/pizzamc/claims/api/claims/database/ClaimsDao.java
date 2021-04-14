package io.github.willqi.pizzamc.claims.api.claims.database;

import io.github.willqi.pizzamc.claims.api.claims.ChunkCoordinates;
import io.github.willqi.pizzamc.claims.api.claims.Claim;

import java.util.Optional;

public interface ClaimsDao {

    Optional<Claim> getClaimByLocation(ChunkCoordinates location);

    void delete(Claim claim);
    void update(Claim claim);
    void insert(Claim claim);

}
