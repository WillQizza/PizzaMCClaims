package io.github.willqi.pizzamc.claims.api.homes;

import io.github.willqi.pizzamc.claims.api.homes.database.HomesDao;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class HomesManager {

    private static final String CREATE_HOMES_TABLES = "CREATE TABLE IF NOT EXISTS homes (" +
            "id INT PRIMARY KEY," +
            "level VARCHAR(36)," +                                          // UUID of the level
            "x INT," +                                                      // x position
            "y INT," +                                                      // y position
            "z INT," +                                                      // z position
            "name VARCHAR(" + Home.MAX_NAME_LENGTH + ")," +                     // name of the home
            "player VARCHAR(36)" +                                          // UUID of the owner of the home
            ")";

    private static final String SELECT_HOME_ID = "SELECT IFNULL(MAX(id) + 1, 0) AS home_id FROM homes";
    private static final String SELECT_HOMES_OF_UUID = "SELECT * FROM homes WHERE player=?";

    private final Map<UUID, Map<String, Home>> cache;

    private final Map<UUID, CompletableFuture<Map<String, Home>>> queueHomeFutures;

    private final HomesDao homesDao;


    public HomesManager (HomesDao homesDao) {
        this.cache = new ConcurrentHashMap<>();
        this.homesDao = homesDao;

        this.queueHomeFutures = new ConcurrentHashMap<>();
    }

    public CompletableFuture<Map<String, Home>> fetchHomes(UUID playerUuid) {
        Optional<Map<String, Home>> existingHomes = this.getHomes(playerUuid);
        if (existingHomes.isPresent()) {
            return CompletableFuture.completedFuture(Collections.unmodifiableMap(existingHomes.get()));
        } else {
            CompletableFuture<Map<String, Home>> returnedFuture = this.queueHomeFutures.getOrDefault(playerUuid, null);
            if (returnedFuture == null) {
                returnedFuture = CompletableFuture.supplyAsync(() -> {
                    Set<Home> homes = this.homesDao.getHomesByOwner(playerUuid);
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

    public Optional<Map<String, Home>> getHomes(UUID playerUuid) {
        return Optional.ofNullable(this.cache.getOrDefault(playerUuid, null));
    }

    public Optional<Home> getHome (UUID uuid, String name) {
        Optional<Map<String, Home>> homes = this.getHomes(uuid);
        if (homes.isPresent()) {
            return Optional.ofNullable(homes.get().getOrDefault(name, null));
        } else {
            return Optional.empty();
        }
    }

    public CompletableFuture<Void> save (Home home) {
        return this.fetchHomes(home.getOwnerUuid()).thenApplyAsync(homes -> {
            Map<String, Home> cachedHomes = Optional.ofNullable(this.cache.getOrDefault(home.getOwnerUuid(), null)).orElseGet(ConcurrentHashMap::new);
            if (cachedHomes.containsKey(home.getName())) {
                this.homesDao.update(home);
            } else {
                this.homesDao.insert(home);
            }
            cachedHomes = this.cache.getOrDefault(home.getOwnerUuid(), cachedHomes);
            cachedHomes.put(home.getName(), home.clone());
            this.cache.putIfAbsent(home.getOwnerUuid(), cachedHomes);
            return null;
        });
    }

    public CompletableFuture<Void> delete(Home home) {
        return CompletableFuture.supplyAsync(() -> {
            Map<String, Home> cachedHomes = this.cache.getOrDefault(home.getOwnerUuid(), null);
            if (cachedHomes != null) {
                cachedHomes.remove(home.getName());
            }
            this.homesDao.delete(home);
            return null;
        });
    }

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
