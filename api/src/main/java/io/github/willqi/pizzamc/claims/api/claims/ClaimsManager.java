package io.github.willqi.pizzamc.claims.api.claims;

import io.github.willqi.pizzamc.claims.api.claims.dao.ClaimsDao;
import io.github.willqi.pizzamc.claims.api.claims.dao.ClaimHelpersDao;
import io.github.willqi.pizzamc.claims.api.exceptions.DaoException;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Responsible for caching claims/helpers and
 * handling interacting with the ClaimsDao/ClaimsHelperDao.
 */
public class ClaimsManager {

    private final Map<ChunkCoordinates, Claim> claimsCache;
    private final Map<ChunkCoordinates, Set<ClaimHelper>> helpersCache;
    private final Map<UUID, Integer> claimCountCache;

    // Used to ensure that only 1 future is active at a time for each query.
    private final Map<ChunkCoordinates, CompletableFuture<Claim>> queueClaimFutures;
    private final Map<ChunkCoordinates, CompletableFuture<Set<ClaimHelper>>> queueHelperFutures;
    private final Map<UUID, CompletableFuture<Integer>> claimCountFutures;

    private final ClaimsDao claimsDao;
    private final ClaimHelpersDao claimHelpersDao;

    public ClaimsManager (ClaimsDao claimsDao, ClaimHelpersDao claimHelpersDao) {
        this.claimsDao = claimsDao;
        this.claimHelpersDao = claimHelpersDao;

        this.claimsCache = new ConcurrentHashMap<>();
        this.helpersCache = new ConcurrentHashMap<>();
        this.claimCountCache = new ConcurrentHashMap<>();

        this.queueClaimFutures = new ConcurrentHashMap<>();
        this.queueHelperFutures = new ConcurrentHashMap<>();
        this.claimCountFutures = new ConcurrentHashMap<>();
    }


    /**
     * Fetch a claim from the ClaimsDao if it's not cached
     * @param coordinates
     * @return CompletableFuture with a empty or existing claim
     */
    public CompletableFuture<Claim> fetchClaim(ChunkCoordinates coordinates) {
        Claim existingClaim = this.claimsCache.getOrDefault(coordinates, null);
        if (existingClaim != null) {
            return CompletableFuture.completedFuture(existingClaim.clone());
        } else {
            // Ensure we don't run unnecessary queries
            CompletableFuture<Claim> returnedFuture = this.queueClaimFutures.getOrDefault(coordinates, null);
            if (returnedFuture == null) {
                returnedFuture = this.fetchClaimHelpers(coordinates).thenApplyAsync(helpers -> {
                    Optional<Claim> result;
                    try {
                        result = this.claimsDao.getClaimByLocation(coordinates);
                    } catch (DaoException exception) {
                        throw new CompletionException(exception);
                    }
                    Claim claim = result.orElseGet(() -> new Claim(coordinates, 0));
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
     * @return cached claim
     */
    public Optional<Claim> getClaim(ChunkCoordinates coordinates) {
        Claim claim = this.claimsCache.getOrDefault(coordinates, null);
        if (claim != null) {
            return Optional.of(claim.clone());
        } else {
            return Optional.empty();
        }
    }

    /**
     * Save a claim to the ClaimsDao
     * @param claim
     * @return CompletableFuture that resolves after saving
     */
    public CompletableFuture<Void> saveClaim(Claim claim) {
        return this.fetchClaim(claim.getCoordinates()).thenAcceptAsync(savedClaim -> {
            try {
                if (savedClaim.getOwner().isPresent() || savedClaim.getFlags() != 0 ) {
                    this.claimsDao.update(claim);
                    this.claimsCache.put(claim.getCoordinates(), claim.clone());
                } else if (claim.getOwner().isPresent() || claim.getFlags() != 0) {
                    this.claimsDao.insert(claim);
                    this.claimsCache.put(claim.getCoordinates(), claim.clone());
                }
            } catch (DaoException exception) {
                throw new CompletionException(exception);
            }

            this.updateClaimCountCache(savedClaim, claim);
        });
    }

    /**
     * Request the ClaimsDao to delete the claim
     * @param claim
     * @return CompletableFuture that resolves after deleting
     */
    public CompletableFuture<Void> deleteClaim(Claim claim) {
        return this.fetchClaimHelpers(claim.getCoordinates())
                .thenAcceptAsync(helpers -> helpers.forEach(helper -> this.deleteClaimHelper(claim.getCoordinates(), helper)))
                .thenRunAsync(() -> this.fetchClaim(claim.getCoordinates()).thenAcceptAsync(cachedClaim -> {
                    try {
                        this.claimsDao.delete(claim);
                    } catch (DaoException exception) {
                        throw new CompletionException(exception);
                    }
                    Claim newClaim = new Claim(claim.getCoordinates(), null, 0);
                    this.claimsCache.put(claim.getCoordinates(), newClaim);

                    this.updateClaimCountCache(cachedClaim, newClaim);
                }));
    }


    /**
     * Fetch the claim helpers of a chunk from the ClaimHelpersDao
     * @param coordinates
     * @return CompletableFuture that resolves with the claim helpers of a chunk
     */
    public CompletableFuture<Set<ClaimHelper>> fetchClaimHelpers(ChunkCoordinates coordinates) {
        Set<ClaimHelper> existingHelpers = this.helpersCache.getOrDefault(coordinates, null);
        if (existingHelpers != null) {
            return CompletableFuture.completedFuture(existingHelpers.stream().map(ClaimHelper::clone).collect(Collectors.toSet()));
        } else {

            // Ensure we don't run unnecessary queries
            CompletableFuture<Set<ClaimHelper>> returnedFuture = this.queueHelperFutures.getOrDefault(coordinates, null);
            if (returnedFuture == null) {
                returnedFuture = CompletableFuture.supplyAsync(() -> {
                    Set<ClaimHelper> helpers;
                    try {
                        helpers = this.claimHelpersDao.getClaimHelpersByLocation(coordinates);
                    } catch (DaoException exception) {
                        throw new CompletionException(exception);
                    }
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

    /**
     * Remove the cached data for a claim.
     * This will also clear claim helpers of a claim.
     * @param coordinates
     */
    public void removeClaimFromCache(ChunkCoordinates coordinates) {
        this.claimsCache.remove(coordinates);
        this.removeClaimHelpersFromCache(coordinates);
    }

    /**
     * Retrieve the cached claim helpers of a chunk.
     * @param coordinates
     * @return cached claim helpers
     */
    public Optional<Set<ClaimHelper>> getClaimHelpers(ChunkCoordinates coordinates) {
        return Optional.ofNullable(this.helpersCache.getOrDefault(coordinates, null))
                .map(helpers -> helpers.stream().map(ClaimHelper::clone).collect(Collectors.toSet()));
    }

    /**
     * Get a claim helper from the cache
     * @param coordinates
     * @param helperUuid
     * @return Will return an empty optional if the claim helper does not exist or if no helpers have been fetched yet.
     */
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

    /**
     * Save a ClaimHelper to the ClaimHelpersDao
     * @param coordinates
     * @param helper
     * @return a CompletableFuture that resolves after saving has finished.
     */
    public CompletableFuture<Void> saveClaimHelper(ChunkCoordinates coordinates, ClaimHelper helper) {
        return this.fetchClaimHelpers(coordinates)
                .thenAcceptAsync(savedHelpers -> {
                    Optional<ClaimHelper> savedHelper = savedHelpers.stream()
                            .filter(h -> h.getUuid().equals(helper.getUuid()))
                            .findAny();
                    try {
                        if (savedHelper.isPresent()) {
                            this.claimHelpersDao.update(coordinates, helper);
                            savedHelper.get().setPermissions(helper.getPermissions());
                            this.helpersCache.put(coordinates, savedHelpers);

                        } else if (helper.getPermissions() != 0) {
                            this.claimHelpersDao.insert(coordinates, helper);
                            savedHelpers.add(helper.clone());
                            this.helpersCache.put(coordinates, savedHelpers);

                        }
                    } catch (DaoException exception) {
                        throw new CompletionException(exception);
                    }
                });
    }

    /**
     * Request deletion of a ClaimHelper to the ClaimHelpersDao
     * @param coordinates
     * @param helper
     * @return a CompletableFuture that resolves after deletion has finished
     */
    public CompletableFuture<Void> deleteClaimHelper(ChunkCoordinates coordinates, ClaimHelper helper) {
        return CompletableFuture.runAsync(() -> {
            try {
                this.claimHelpersDao.delete(coordinates, helper);
            } catch (DaoException exception) {
                throw new CompletionException(exception);
            }
            Set<ClaimHelper> helpers = this.helpersCache.getOrDefault(coordinates, null);
            if (helpers != null) {
                helpers.remove(helper);
            }
        });
    }

    /**
     * Remove all claim helpers cached for a coordinate
     * @param coordinates
     */
    public void removeClaimHelpersFromCache(ChunkCoordinates coordinates) {
        this.helpersCache.remove(coordinates);
    }

    /**
     * Retrieve the amount of claims a player has from the ClaimHelpersDao
     * @param uuid
     * @return the amount of claims a user has
     */
    public CompletableFuture<Integer> fetchClaimCount(UUID uuid) {
        Integer count = this.claimCountCache.getOrDefault(uuid, null);
        if (count != null) {
            return CompletableFuture.completedFuture(count);
        } else {
            CompletableFuture<Integer> countFuture = this.claimCountFutures.getOrDefault(uuid, null);
            if (countFuture != null) {
                return countFuture;
            } else {
                CompletableFuture<Integer> activeCountFuture = CompletableFuture.supplyAsync(() -> {
                    try {
                        int claims = this.claimsDao.getClaimCountOfUuid(uuid);
                        this.claimCountCache.put(uuid, claims);
                        this.claimCountFutures.remove(uuid);
                        return claims;
                    } catch (DaoException exception) {
                        throw new CompletionException(exception);
                    }
                });
                this.claimCountFutures.put(uuid, activeCountFuture);
                return activeCountFuture;
            }
        }
    }

    /**
     * Retrieve the amount of claims a player has from the cache
     * @param uuid
     * @return cached claim count
     */
    public Optional<Integer> getClaimCount(UUID uuid) {
        return Optional.ofNullable(this.claimCountCache.getOrDefault(uuid, null));
    }

    /**
     * Remove cached claim count for a player
     * @param uuid
     */
    public void removeClaimCountFromCache(UUID uuid) {
        this.claimCountCache.remove(uuid);
    }

    /**
     * Update the claim count of a player.
     * Note: This will not update anything if the player's claim count was not fetched first
     * @param oldClaim The old state of the claim being updated
     * @param newClaim The new state of the claim being updated
     */
    private void updateClaimCountCache(Claim oldClaim, Claim newClaim) {
        if (oldClaim.getOwner().isPresent() && !newClaim.getOwner().isPresent()) {
            // Removing owner from claim
            this.decrementClaimCountIfAvailable(oldClaim.getOwner().get());
        } else if (!oldClaim.getOwner().isPresent() && newClaim.getOwner().isPresent()) {
            // Setting owner to empty claim
            this.incrementClaimCountIfAvailable(newClaim.getOwner().get());
        } else if ( (!oldClaim.getOwner().equals(newClaim.getOwner())) && (oldClaim.getOwner().isPresent() && newClaim.getOwner().isPresent()) ) {
            // Updating owner
            this.decrementClaimCountIfAvailable(oldClaim.getOwner().get());
            this.incrementClaimCountIfAvailable(newClaim.getOwner().get());
        }
    }

    private void incrementClaimCountIfAvailable(UUID uuid) {
        this.claimCountCache.computeIfPresent(uuid, (key, currentClaimCunt) -> currentClaimCunt + 1);
    }

    private void decrementClaimCountIfAvailable(UUID uuid) {
        this.claimCountCache.computeIfPresent(uuid, (key, currentClaimCunt) -> currentClaimCunt - 1);
    }


    /**
     * Called internally when plugin is shutdown
     */
    public void cleanUp () {
        this.claimsCache.clear();
        this.helpersCache.clear();
        this.claimCountCache.clear();

        this.queueHelperFutures.clear();
        this.queueClaimFutures.clear();
        this.claimCountFutures.clear();
    }

}
