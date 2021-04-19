package io.github.willqi.pizzamc.claims.api.claims;

import io.github.willqi.pizzamc.claims.api.claims.dao.ClaimsDao;
import io.github.willqi.pizzamc.claims.api.claims.dao.ClaimsHelperDao;
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
                    Set<ClaimHelper> helpers;
                    try {
                        helpers = this.claimsHelperDao.getClaimHelpersByLocation(coordinates);
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
        });
    }

    public CompletableFuture<Void> deleteClaim(Claim claim) {
        return this.fetchClaimHelpers(claim.getCoordinates())
                .thenAcceptAsync(helpers -> helpers.forEach(helper -> this.deleteClaimHelper(claim.getCoordinates(), helper)))
                .thenRunAsync(() -> {
                    try {
                        this.claimsDao.delete(claim);
                    } catch (DaoException exception) {
                        throw new CompletionException(exception);
                    }
                    this.claimsCache.put(claim.getCoordinates(), new Claim(claim.getCoordinates(), null, 0));
                });
    }

    public CompletableFuture<Void> saveClaimHelper(ChunkCoordinates coordinates, ClaimHelper helper) {
        return this.fetchClaimHelpers(coordinates)
                .thenAcceptAsync(savedHelpers -> {
                    Optional<ClaimHelper> savedHelper = savedHelpers.stream()
                            .filter(h -> h.getUuid().equals(helper.getUuid()))
                            .findAny();
                    try {
                        if (savedHelper.isPresent()) {
                            this.claimsHelperDao.update(coordinates, helper);
                            savedHelper.get().setPermissions(helper.getPermissions());
                            this.helpersCache.put(coordinates, savedHelpers);

                        } else if (helper.getPermissions() != 0) {
                            this.claimsHelperDao.insert(coordinates, helper);
                            savedHelpers.add(helper.clone());
                            this.helpersCache.put(coordinates, savedHelpers);

                        }
                    } catch (DaoException exception) {
                        throw new CompletionException(exception);
                    }
                });
    }

    public CompletableFuture<Void> deleteClaimHelper(ChunkCoordinates coordinates, ClaimHelper helper) {
        return CompletableFuture.runAsync(() -> {
            try {
                this.claimsHelperDao.delete(coordinates, helper);
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
     * Called internally when plugin is shutdown
     */
    public void cleanUp () {
        this.claimsCache.clear();
        this.queueHelperFutures.clear();
        this.queueClaimFutures.clear();
    }

}
