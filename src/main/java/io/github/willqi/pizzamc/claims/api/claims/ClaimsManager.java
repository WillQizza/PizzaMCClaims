package io.github.willqi.pizzamc.claims.api.claims;

import io.github.willqi.pizzamc.claims.api.claims.database.ClaimsDao;
import io.github.willqi.pizzamc.claims.api.claims.database.ClaimsHelperDao;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

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
            return CompletableFuture.completedFuture(existingHelpers);
        } else {
            return CompletableFuture.supplyAsync(() -> {
                Set<ClaimHelper> helpers = this.claimsHelperDao.getClaimHelpersByLocation(coordinates);
                this.helpersCache.putIfAbsent(coordinates, helpers);
                return helpers;
            });
        }
    }

    public Optional<Set<ClaimHelper>> getClaimHelpers(ChunkCoordinates coordinates) {
        return Optional.ofNullable(this.helpersCache.getOrDefault(coordinates, null));
    }

    public CompletableFuture<Optional<ClaimHelper>> fetchClaimHelper(ChunkCoordinates coordinates, UUID helperUuid) {
        Set<ClaimHelper> existingHelpers = this.helpersCache.getOrDefault(coordinates, null);
        if (existingHelpers != null) {
            return CompletableFuture.completedFuture(
                    existingHelpers.stream()
                        .filter(helper -> helper.getUuid().equals(helperUuid))
                        .findAny()
            );
        } else {
            return CompletableFuture.supplyAsync(() -> {
                Optional<ClaimHelper> helper = this.claimsHelperDao.getClaimHelperByLocation(coordinates);
                Set<ClaimHelper> currentExistingHelpers = this.helpersCache.getOrDefault(coordinates, null);
                if (currentExistingHelpers == null) {
                    currentExistingHelpers = this.helpersCache.putIfAbsent(coordinates, ConcurrentHashMap.newKeySet());
                }
                if (helper.isPresent()) {
                    currentExistingHelpers.add(helper.get());
                } else {
                    currentExistingHelpers.removeIf(h -> h.getUuid().equals(helperUuid));
                }
                return helper;
            });
        }
    }

    public Optional<ClaimHelper> getClaimHelper(ChunkCoordinates coordinates, UUID helperUuid) {
        Set<ClaimHelper> existingHelpers = this.helpersCache.getOrDefault(coordinates, null);
        if (existingHelpers != null) {
            return existingHelpers.stream()
                    .filter(helper -> helper.getUuid().equals(helperUuid))
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
                if (claim.getOwner().isPresent()) {
                    this.claimsDao.update(claim);
                } else {
                    this.claimsDao.delete(claim);
                }
            } else if (claim.getOwner().isPresent()) {
                this.claimsDao.insert(claim);
            } else {
                return false;
            }
            return true;
        });
    }

    public CompletableFuture<Boolean> saveClaimHelper(Claim claim, ClaimHelper helper) {
        return CompletableFuture.supplyAsync(() -> {
            Optional<ClaimHelper> savedHelper;
            try {
                savedHelper = this.fetchClaimHelper(claim, helper.getUuid()).get();
            } catch (InterruptedException | ExecutionException exception) {
                exception.printStackTrace();
                return false;
            }
            if (savedHelper.isPresent()) {
                if (helper.getPermissions() == 0) {
                    this.claimsHelperDao.delete(claim, helper);
                } else {
                    this.claimsHelperDao.update(claim, helper);
                }
            } else {
                this.claimsHelperDao.insert(claim, helper);
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
