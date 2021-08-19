package io.github.willqi.pizzamc.claims.api.homes;

import io.github.willqi.pizzamc.claims.api.exceptions.DaoException;
import io.github.willqi.pizzamc.claims.api.homes.dao.HomesDao;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Responsible for homes and handling interactions with the HomesDao
 */
public class HomesManager {

    private final Map<UUID, Map<String, Home>> cache;

    private final Map<UUID, CompletableFuture<Map<String, Home>>> queueHomeFutures;

    private final HomesDao homesDao;


    public HomesManager (HomesDao homesDao) {
        this.cache = new ConcurrentHashMap<>();
        this.homesDao = homesDao;

        this.queueHomeFutures = new ConcurrentHashMap<>();
    }

    /**
     * Fetch the homes of a user from the HomesDao
     * This will use the cache if it is available
     * @param playerUuid
     * @return the homes of the player
     */
    public CompletableFuture<Map<String, Home>> fetchHomes(UUID playerUuid) {
        Optional<Map<String, Home>> existingHomes = this.getHomes(playerUuid);
        if (existingHomes.isPresent()) {
            Map<String, Home> returnedHomes = new HashMap<>(existingHomes.get());
            returnedHomes.replaceAll((name, home) -> home.clone());
            return CompletableFuture.completedFuture(Collections.unmodifiableMap(returnedHomes));
        } else {
            CompletableFuture<Map<String, Home>> returnedFuture = this.queueHomeFutures.getOrDefault(playerUuid, null);
            if (returnedFuture == null) {

                returnedFuture = CompletableFuture.supplyAsync(() -> {
                    Set<Home> homes;
                    try {
                        homes = this.homesDao.getHomesByOwner(playerUuid);
                    } catch (DaoException exception) {
                        throw new RuntimeException(exception);
                    }
                    Map<String, Home> mappedHomes = new ConcurrentHashMap<>();
                    for (Home home : homes) {
                        mappedHomes.put(home.getName(), home);
                    }
                    this.cache.putIfAbsent(playerUuid, mappedHomes);

                    Map<String, Home> returnedMappedHomes = new HashMap<>(mappedHomes);
                    returnedMappedHomes.replaceAll((name, home) -> home.clone());
                    this.queueHomeFutures.remove(playerUuid);
                    return Collections.unmodifiableMap(returnedMappedHomes);
                });
                this.queueHomeFutures.put(playerUuid, returnedFuture);

            }
            return returnedFuture;
        }
    }

    /**
     * Get the cached homes of a player
     * @param playerUuid
     * @return the cached homes
     */
    public Optional<Map<String, Home>> getHomes(UUID playerUuid) {
        Map<String, Home> cachedMap = this.cache.getOrDefault(playerUuid, null);
        if (cachedMap != null) {
            Map<String, Home> returnedMap = new HashMap<>(cachedMap);
            returnedMap.replaceAll((name, home) -> home.clone());
            return Optional.of(Collections.unmodifiableMap(returnedMap));
        } else {
            return Optional.empty();
        }
    }

    /**
     * Get a specific home of a player if it exists
     * @param ownerUuid
     * @param name This is case sensitive
     * @return an empty optional if the player's homes were not fetched or if the player does not have a home named that
     */
    public Optional<Home> getHome (UUID ownerUuid, String name) {
        Optional<Map<String, Home>> homes = this.getHomes(ownerUuid);
        if (homes.isPresent()) {
            return Optional.ofNullable(homes.get().getOrDefault(name, null));
        } else {
            return Optional.empty();
        }
    }

    /**
     * Save a home to the HomesDao
     * @param home
     * @return a CompletableFuture that resolves after saving
     */
    public CompletableFuture<Void> save (Home home) {
        return this.fetchHomes(home.getOwnerUUID()).thenAcceptAsync(homes -> {
            Map<String, Home> cachedHomes = Optional.ofNullable(this.cache.getOrDefault(home.getOwnerUUID(), null)).orElseGet(ConcurrentHashMap::new);
            try {
                if (cachedHomes.containsKey(home.getName())) {
                    this.homesDao.update(home);
                } else {
                    this.homesDao.insert(home);
                }
            } catch (DaoException exception) {
                throw new CompletionException(exception);
            }
            cachedHomes = this.cache.getOrDefault(home.getOwnerUUID(), cachedHomes);
            cachedHomes.put(home.getName(), home.clone());
            this.cache.putIfAbsent(home.getOwnerUUID(), cachedHomes);
        });
    }

    /**
     * Request deletion of a home to the HomesDao
     * @param home
     * @return a CompletableFuture that resolves after deletion
     */
    public CompletableFuture<Void> delete(Home home) {
        return CompletableFuture.runAsync(() -> {
            Map<String, Home> cachedHomes = this.cache.getOrDefault(home.getOwnerUUID(), null);
            if (cachedHomes != null) {
                cachedHomes.remove(home.getName());
            }
            try {
                this.homesDao.delete(home);
            } catch (DaoException exception) {
                throw new CompletionException(exception);
            }
        });
    }

    /**
     * Clear the homes cache of a player
     * @param uuid
     */
    public void clearHomesCache (UUID uuid) {
        this.cache.remove(uuid);
    }

    /**
     * Called internally when plugin is shutdown
     */
    public void cleanUp () {
        this.cache.clear();
        this.queueHomeFutures.clear();
    }

}
