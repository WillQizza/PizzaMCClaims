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

    // Used to ensure that only 1 coordinate is being fetched at a time
    private final Map<ChunkCoordinates, CompletableFuture<Claim>> queueClaimFutures;
    private final Map<ChunkCoordinates, CompletableFuture<Set<ClaimHelper>>> queueHelperFutures;

    private final ClaimsDao claimsDao;
    private final ClaimsHelperDao claimsHelperDao;

    public ClaimsManager (ClaimsDao claimsDao, ClaimsHelperDao claimsHelperDao) {
        this.claimsDao = claimsDao;
        this.claimsHelperDao = claimsHelperDao;

        this.claimsCache = new ConcurrentHashMap<>();
        this.helpersCache = new ConcurrentHashMap<>();

        this.queueClaimFutures = new ConcurrentHashMap<>();
        this.queueHelperFutures = new ConcurrentHashMap<>();
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
            // Ensure we don't run unnecessary queries
            CompletableFuture<Claim> returnedFuture = this.queueClaimFutures.getOrDefault(coordinates, null);
            if (returnedFuture == null) {
                returnedFuture = CompletableFuture.supplyAsync(() -> {
                    Optional<Claim> result = this.claimsDao.getClaimByLocation(coordinates);
                    Claim claim = result.orElseGet(() -> new Claim(coordinates.getWorldUuid(), coordinates.getX(), coordinates.getZ(), 0));
                    this.claimsCache.putIfAbsent(coordinates, claim);
                    this.queueClaimFutures.remove(coordinates);
                    return claim.clone();
                });
                this.queueClaimFutures.putIfAbsent(coordinates, returnedFuture);
            }
            return returnedFuture;
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

            // Ensure we don't run unnecessary queries
            CompletableFuture<Set<ClaimHelper>> returnedFuture = this.queueHelperFutures.getOrDefault(coordinates, null);
            if (returnedFuture == null) {
                returnedFuture = CompletableFuture.supplyAsync(() -> {
                    Set<ClaimHelper> helpers = this.claimsHelperDao.getClaimHelpersByLocation(coordinates);
                    this.helpersCache.putIfAbsent(coordinates, helpers);
                    this.queueHelperFutures.remove(coordinates);
                    return helpers.stream()
                            .map(ClaimHelper::clone)
                            .collect(Collectors.toSet());
                });
                this.queueHelperFutures.putIfAbsent(coordinates, returnedFuture);
            }
            return returnedFuture;
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
                throw new RuntimeException(exception);
            }
            if (savedClaim.getOwner().isPresent() || savedClaim.getFlags() != 0 ) {
                this.claimsDao.update(claim);
            } else if (claim.getOwner().isPresent() || claim.getFlags() != 0) {
                this.claimsDao.insert(claim);
            } else {
                return false;
            }
            this.claimsCache.put(claim, claim.clone());
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
        return this.fetchClaimHelpers(coordinates)
                .thenApplyAsync(savedHelpers -> {
                    Optional<ClaimHelper> savedHelper = savedHelpers.stream()
                            .filter(h -> h.getUuid().equals(helper.getUuid()))
                            .findAny();
                    if (savedHelper.isPresent()) {
                        this.claimsHelperDao.update(coordinates, helper);
                        savedHelper.get().setPermissions(helper.getPermissions());
                    } else if (helper.getPermissions() == 0) {
                        return false;   // Don't save an empty helper that isn't in the database
                    } else {
                        this.claimsHelperDao.insert(coordinates, helper);
                        savedHelpers.add(helper.clone());
                    }
                    this.helpersCache.put(coordinates, savedHelpers);
                    return true;
                });
    }

    public CompletableFuture<Void> deleteClaimHelper(ChunkCoordinates coordinates, ClaimHelper helper) {
        return CompletableFuture.supplyAsync(() -> {
            this.claimsHelperDao.delete(coordinates, helper);
            Set<ClaimHelper> helpers = this.helpersCache.getOrDefault(coordinates, null);
            if (helpers != null) {
                helpers.remove(helper);
            }
            return null;
        });
    }


    /**
     * Called internally when plugin is shutdown
     */
    public void cleanUp () {
        this.claimsCache.clear();
        this.queueHelperFutures.clear();
        this.queueClaimFutures.clear();
    }

}
