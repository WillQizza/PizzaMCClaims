package io.github.willqi.pizzamc.claims.api.claims;

import io.github.willqi.pizzamc.claims.api.claims.database.ClaimsDao;
import io.github.willqi.pizzamc.claims.api.claims.database.ClaimsHelperDao;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * Responsible for caching claims/helpers and
 * handling interacting with the ClaimsDao/ClaimsHelperDao.
 */
public class ClaimsManager {

    private static final String CREATE_CLAIMS_TABLE = "CREATE TABLE IF NOT EXISTS claims (" +
                                                        "id INT PRIMARY KEY," +
                                                        "level VARCHAR(36)," +                // UUID of the level
                                                        "x INT," +                             // Chunk X
                                                        "z INT," +                             // Chunk Y
                                                        "flags INT," +                         // Extra features for the chunk
                                                        "player VARCHAR(36)" +                 // UUID of the owner of the chunk
                                                        ")";
    private static final String CREATE_CLAIM_HELPERS_TABLE = "CREATE TABLE IF NOT EXISTS claim_helpers (" +
                                                        "id INT PRIMARY KEY," +
                                                        "claim_id INT," +               // Corresponding claim id
                                                        "permissions INT," +            // Permissions the helper has.
                                                        "player VARCHAR(36)" +          // UUID of the owner of the chunk
                                                        ")";

    private static final String SELECT_CLAIMS_ID = "SELECT IFNULL(MAX(id) + 1, 0) AS claim_id FROM claims";
    private static final String SELECT_CLAIM_HELPERS_ID = "SELECT IFNULL(MAX(id) + 1, 0) AS helper_id FROM claim_helpers";

    private static final String SELECT_CLAIMS = "SELECT * FROM claims";
    private static final String SELECT_CLAIM_HELPERS = "SELECT * FROM claim_helpers";

    private final Map<ChunkCoordinates, Claim> claimsCache;
    private final Map<ChunkCoordinates, Set<ClaimHelper>> helpersCache;

    private final ClaimsDao claimsDao;
    private final ClaimsHelperDao claimsHelperDao;

    public ClaimsManager (ClaimsDao claimsDao, ClaimsHelperDao claimsHelperDao) {
        this.claimsDao = claimsDao;
        this.claimsHelperDao = claimsHelperDao;

        this.claimsCache = new ConcurrentHashMap<>();
        this.helpersCache = new ConcurrentHashMap<>();
    }




    /**
     * Fetch a claim from the database if it's not cached
     * @param coordinates
     * @return the claim data
     */
    public CompletableFuture<Claim> fetchClaim(ChunkCoordinates coordinates) {
        Claim existingClaim = this.claimsCache.getOrDefault(coordinates, null);
        if (existingClaim != null) {
            return CompletableFuture.completedFuture(existingClaim);
        } else {
            return CompletableFuture.supplyAsync(() -> {
                Optional<Claim> result = this.claimsDao.getClaimByLocation(new ChunkCoordinates(coordinates.getWorldUuid(), coordinates.getX(), coordinates.getZ()));
                Claim claim = result.orElseGet(() -> new Claim(coordinates.getWorldUuid(), coordinates.getX(), coordinates.getZ()));
                this.claimsCache.putIfAbsent(coordinates, claim);
                return claim.clone();
            });
        }
    }

    /**
     * Get a claim from the cache if it is cached.
     * @param coordinates
     * @return
     */
    public Optional<Claim> getClaim(ChunkCoordinates coordinates) {
        Claim claim = this.claimsCache.getOrDefault(coordinates, null);
        if (claim != null) {
            return Optional.of(claim.clone());
        } else {
            return Optional.empty();
        }
    }

    public void removeClaimFromCache(ChunkCoordinates coordinates) {
        this.claimsCache.remove(coordinates);
        this.removeClaimHelpersFromCache(coordinates);
    }



    public CompletableFuture<Set<ClaimHelper>> fetchClaimHelpers(ChunkCoordinates coordinates) {
        Set<ClaimHelper> existingHelpers = this.helpersCache.getOrDefault(coordinates, null);
        if (existingHelpers != null) {
            return CompletableFuture.completedFuture(existingHelpers.stream().map(ClaimHelper::clone).collect(Collectors.toSet()));
        } else {
            return CompletableFuture.supplyAsync(() -> {
                Set<ClaimHelper> helpers = this.claimsHelperDao.getClaimHelpersByLocation(coordinates);
                this.helpersCache.putIfAbsent(coordinates, helpers);
                return helpers.stream()
                        .map(ClaimHelper::clone)
                        .collect(Collectors.toSet());
            });
        }
    }

    public Optional<Set<ClaimHelper>> getClaimHelpers(ChunkCoordinates coordinates) {
        return Optional.ofNullable(this.helpersCache.getOrDefault(coordinates, null))
                .map(helpers -> helpers.stream().map(ClaimHelper::clone).collect(Collectors.toSet()));
    }

    public Optional<ClaimHelper> getClaimHelper(ChunkCoordinates coordinates, UUID helperUuid) {
        Set<ClaimHelper> existingHelpers = this.helpersCache.getOrDefault(coordinates, null);
        if (existingHelpers != null) {
            return existingHelpers.stream()
                    .filter(helper -> helper.getUuid().equals(helperUuid))
                    .map(ClaimHelper::clone)
                    .findAny();
        } else {
            return Optional.empty();
        }
    }

    public void removeClaimHelpersFromCache(ChunkCoordinates coordinates) {
        this.helpersCache.remove(coordinates);
    }





    public CompletableFuture<Boolean> saveClaim(Claim claim) {
        return CompletableFuture.supplyAsync(() -> {
            Claim savedClaim;
            try {
                savedClaim = this.fetchClaim(claim).get();
            } catch (InterruptedException | ExecutionException exception) {
                exception.printStackTrace();
                return false;
            }
            if (savedClaim.getOwner().isPresent()) {
                this.claimsDao.update(claim);
            } else if (claim.getOwner().isPresent()) {
                this.claimsDao.insert(claim);
            } else {
                return false;
            }
            this.claimsCache.put(claim, claim);
            return true;
        });
    }

    public CompletableFuture<Boolean> deleteClaim(Claim claim) {
        return CompletableFuture.supplyAsync(() -> {
            this.claimsDao.delete(claim);
            this.claimsCache.remove(claim);
            return true;
        });
    }

    public CompletableFuture<Boolean> saveClaimHelper(ChunkCoordinates coordinates, ClaimHelper helper) {
        return CompletableFuture.supplyAsync(() -> {
            Set<ClaimHelper> savedHelpers;
            try {
                savedHelpers = this.fetchClaimHelpers(coordinates).get();
            } catch (InterruptedException | ExecutionException exception) {
                exception.printStackTrace();
                return false;
            }
            Optional<ClaimHelper> savedHelper = savedHelpers.stream()
                    .filter(h -> h.getUuid().equals(helper.getUuid()))
                    .findAny();
            if (savedHelper.isPresent()) {
                this.claimsHelperDao.update(coordinates, helper);
                savedHelper.get().setPermissions(helper.getPermissions());
            } else {
                this.claimsHelperDao.insert(coordinates, helper);
                savedHelpers.add(helper.clone());
            }
            this.helpersCache.put(coordinates, savedHelpers);
            return true;
        });
    }

    public CompletableFuture<Boolean> deleteClaimHelper(ChunkCoordinates coordinates, ClaimHelper helper) {
        return CompletableFuture.supplyAsync(() -> {
            this.claimsHelperDao.delete(coordinates, helper);
            Set<ClaimHelper> helpers = this.helpersCache.getOrDefault(coordinates, null);
            if (helpers != null) {
                helpers.remove(helper);
            }
            return true;
        });
    }


    /**
     * Called internally when plugin is shutdown
     */
    public void cleanUp () {
        this.claimsCache.clear();
    }

}
