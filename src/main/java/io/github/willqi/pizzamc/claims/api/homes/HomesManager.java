package io.github.willqi.pizzamc.claims.api.homes;

import io.github.willqi.pizzamc.claims.ClaimsPlugin;
import io.github.willqi.pizzamc.claims.api.homes.exceptions.HomesNotLoadedException;
import io.github.willqi.pizzamc.claims.api.homes.exceptions.InvalidHomeNameException;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.logging.Level;

/**
 * Used to interact with homes
 */
public class HomesManager {

    public static int HOME_NAME_LENGTH = 50;                                // NOTE: Modifying this value requires you to update existing tables.

    private static final String CREATE_HOMES_TABLES = "CREATE TABLE IF NOT EXISTS homes (" +
            "id INT PRIMARY KEY," +
            "level VARCHAR(36)," +                                          // UUID of the level
            "x INT," +                                                      // x position
            "y INT," +                                                      // y position
            "z INT," +                                                      // z position
            "name VARCHAR(" + HOME_NAME_LENGTH + ")," +                     // name of the home
            "player VARCHAR(36)" +                                          // UUID of the owner of the home
            ")";

    private static final String SELECT_HOME_ID = "SELECT IFNULL(MAX(id) + 1, 0) AS home_id FROM homes";
    private static final String SELECT_HOMES_OF_UUID = "SELECT * FROM homes WHERE player=?";

    private final ClaimsPlugin plugin;

    private int HOME_ID = 0;

    private Map<UUID, NavigableMap<Integer, Home>> homes = new ConcurrentHashMap<>();

    public HomesManager (final ClaimsPlugin plugin) {
        this.plugin = plugin;
        initSQLQueries();
    }

    /**
     * Load all homes owned by a player
     * @param player
     */
    public void loadHomes (final Player player) {
        loadHomes(player.getUniqueId());
    }

    /**
     * Load all homes owned by a player
     * @param uuid
     */
    public void loadHomes (final UUID uuid) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                synchronized (plugin.getDatabase().getConnection()) {
                    if (!homes.containsKey(uuid)) {
                        final NavigableMap<Integer, Home> playerHomes = new ConcurrentSkipListMap<>();
                        PreparedStatement stmt = null;
                        try {
                            stmt = plugin.getDatabase().getConnection().prepareStatement(SELECT_HOMES_OF_UUID);
                            stmt.setString(1, uuid.toString());
                            final ResultSet results = stmt.executeQuery();
                            while (results.next()) {
                                final Home home = new Home(
                                        results.getInt("id"),
                                        plugin.getServer().getPlayer(uuid),
                                        UUID.fromString(results.getString("level")),
                                        results.getString("name"),
                                        results.getInt("x"),
                                        results.getInt("y"),
                                        results.getInt("z"),
                                        true
                                );
                                playerHomes.put(home.getId(), home);
                            }
                        } catch (SQLException exception) {
                            plugin.getServer().getLogger().log(Level.WARNING, "Failed to fetch homes for UUID: " + uuid);
                        } finally {
                            if (stmt != null) {
                                try {
                                    stmt.close();
                                } catch (SQLException exception) {
                                }
                            }
                        }
                        homes.put(uuid, playerHomes);
                    }
                }
        });
    }

    /**
     * Unload all the homes owned by a player
     * @param player
     */
    public void unloadHomes (final Player player) {
        unloadHomes(player.getUniqueId());
    }

    /**
     * Unload all the homes owned by a player
     * @param uuid
     */
    public void unloadHomes (final UUID uuid) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            if (homes.containsKey(uuid)) {
                saveHomeUUIDData(uuid);
                homes.remove(uuid);
            }
        });
    }

    /**
     * Checks if the data for a player has been loaded
     * @param player
     * @return if it has
     */
    public boolean hasHomesLoaded (final Player player) {
        return hasHomesLoaded(player.getUniqueId());
    }

    /**
     * Checks if the data for a UUID has been loaded
     * @param uuid
     * @return if it has
     */
    public boolean hasHomesLoaded (final UUID uuid) {
        return homes.containsKey(uuid);
    }

    /**
     * Retrieve all the homes owned by a player
     * @param player
     * @return A map of all the homes owned by a player indexed by their house name.
     */
    public NavigableMap<Integer, Home> getHomes (final Player player) throws HomesNotLoadedException {
        return getHomes(player.getUniqueId());
    }

    /**
     * Retrieve all the homes owned by a player
     * @param uuid
     * @return A map of all the homes owned by a player indexed by their house name.
     */
    public NavigableMap<Integer, Home> getHomes (final UUID uuid) throws HomesNotLoadedException {

        if (!hasHomesLoaded(uuid)) {
            throw new HomesNotLoadedException(
                    String.format("Data was not loaded for %s when fetching homes.", uuid)
            );
        }

        return homes.get(uuid);
    }

    /**
     * Check if a player with loaded data can create new homes
     * @param player The player
     * @return
     */
    public boolean canCreateHome (final Player player) throws HomesNotLoadedException {

        if (!hasHomesLoaded(player)) {
            throw new HomesNotLoadedException(
                    String.format("Data was not loaded for %s when fetching homes.", player.getUniqueId())
            );
        }

        final int maxHomes = plugin.getConfig().getInt("max_homes_per_player");
        if (maxHomes == -1) {
            return true; // Infinity
        }
        final NavigableMap<Integer, Home> playerHomesAmount = getHomes(player);
        return playerHomesAmount.keySet().size() < maxHomes;
    }

    /**
     * Creates a home
     * @param player
     * @param name
     */
    public void createHome (final Player player, final String name) throws HomesNotLoadedException, InvalidHomeNameException {

        if (!hasHomesLoaded(player)) {
            throw new HomesNotLoadedException(
                    String.format("Tried to create a home for %s before data was loaded.", player.getUniqueId())
            );
        }

        if (name.length() > HOME_NAME_LENGTH) {
            throw new InvalidHomeNameException(
                    String.format("Homes cannot be longer than %s characters", HOME_NAME_LENGTH)
            );
        }

        final Home home = new Home(
                HOME_ID++,
                player,
                player.getWorld().getUID(),
                name,
                (int)player.getLocation().getX(),
                (int)player.getLocation().getY(),
                (int)player.getLocation().getZ(),
                false
        );

        final NavigableMap<Integer, Home> playerMaps = homes.get(player.getUniqueId());
        playerMaps.put(home.getId(), home);
        homes.put(player.getUniqueId(), playerMaps);
    }

    /**
     * Creates required SQL tables and get HOME_ID
     */
    private void initSQLQueries() {
        synchronized (plugin.getDatabase().getConnection()) {
            Statement stmt = null;
            try {
                stmt = plugin.getDatabase().getConnection().createStatement();
                stmt.execute(CREATE_HOMES_TABLES);
                final ResultSet idResults = stmt.executeQuery(SELECT_HOME_ID);
                idResults.next(); // Impossible to not have a row.
                HOME_ID = idResults.getInt("home_id");
            } catch (SQLException exception) {
                exception.printStackTrace();
                plugin.getLogger().log(Level.SEVERE, "Failed to create required tables! Disabling...");
                plugin.getPluginLoader().disablePlugin(plugin);
            } finally {
                if (stmt != null) {
                    try {
                        stmt.close();
                    } catch (SQLException exception) { }
                }
            }
        }
    }

    /**
     * Unload all data.
     */
    public void cleanUp () {

        final Iterator<UUID> uuidIterator = homes.keySet().iterator();
        while (uuidIterator.hasNext()) {
            saveHomeUUIDData(
                    uuidIterator.next()
            );
            uuidIterator.remove();
        }

    }

    /**
     * Called internally to save home data
     * @param uuid
     */
    private void saveHomeUUIDData (final UUID uuid) {
        for (final Home home : homes.get(uuid).values()) {
            if (home.isModified()) {
                home.save();
            }
        }
    }

}
