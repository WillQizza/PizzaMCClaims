package io.github.willqi.pizzamc.claims.api.homes;

import io.github.willqi.pizzamc.claims.api.exceptions.DaoException;
import io.github.willqi.pizzamc.claims.api.homes.dao.HomesDao;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
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

    public Optional<Home> getHome (UUID ownerUuid, String name) {
        Optional<Map<String, Home>> homes = this.getHomes(ownerUuid);
        if (homes.isPresent()) {
            return Optional.ofNullable(homes.get().getOrDefault(name, null));
        } else {
            return Optional.empty();
        }
    }

    public CompletableFuture<Void> save (Home home) {
        return this.fetchHomes(home.getOwnerUuid()).thenAcceptAsync(homes -> {
            Map<String, Home> cachedHomes = Optional.ofNullable(this.cache.getOrDefault(home.getOwnerUuid(), null)).orElseGet(ConcurrentHashMap::new);
            try {
                if (cachedHomes.containsKey(home.getName())) {
                    this.homesDao.update(home);
                } else {
                    this.homesDao.insert(home);
                }
            } catch (DaoException exception) {
                throw new CompletionException(exception);
            }
            cachedHomes = this.cache.getOrDefault(home.getOwnerUuid(), cachedHomes);
            cachedHomes.put(home.getName(), home.clone());
            this.cache.putIfAbsent(home.getOwnerUuid(), cachedHomes);
        });
    }

    public CompletableFuture<Void> delete(Home home) {
        return CompletableFuture.runAsync(() -> {
            Map<String, Home> cachedHomes = this.cache.getOrDefault(home.getOwnerUuid(), null);
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
