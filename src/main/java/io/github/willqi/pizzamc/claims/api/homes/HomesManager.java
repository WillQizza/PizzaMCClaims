package io.github.willqi.pizzamc.claims.api.homes;

import io.github.willqi.pizzamc.claims.ClaimsPlugin;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Used to interact with homes
 */
public class HomesManager {

    private static final String CREATE_HOMES_TABLES = "CREATE TABLE IF NOT EXISTS homes (" +
            "id INT PRIMARY KEY," +
            "level VAR(36)," +                      // UUID of the level
            "x INT," +                              // x position
            "y INT," +                              // y position
            "z INT," +                              // z position
            "name VARCHAR(50)," +                   // name of the home
            "player VARCHAR(36)" +                  // UUID of the owner of the home
            ")";

    private final ClaimsPlugin plugin;

    private Map<String, Home> homes = new ConcurrentHashMap<>();

    public HomesManager (ClaimsPlugin plugin) {
        this.plugin = plugin;
        setupTables();
    }

    /**
     * Load all homes owned by a player
     * @param player
     */
    public void loadHomes (OfflinePlayer player) {
        loadHomes(player.getUniqueId());
    }

    /**
     * Load all homes owned by a player
     * @param uuid
     */
    public void loadHomes (UUID uuid) {

    }

    /**
     * Unload all the homes owned by a player
     * @param player
     */
    public void unloadHomes (OfflinePlayer player) {
        unloadHomes(player.getUniqueId());
    }

    /**
     * Unload all the homes owned by a player
     * @param uuid
     */
    public void unloadHomes (UUID uuid) {

    }

    /**
     * Retrieve all the homes owned by a player
     * @param player
     * @return A list of all the homes owned by a player.
     */
    public Optional<List<Home>> getHomes (OfflinePlayer player) {
        return getHomes(player.getUniqueId());
    }

    /**
     * Retrieve all the homes owned by a player
     * @param uuid
     * @return A list of all the homes owned by a player.
     */
    public Optional<List<Home>> getHomes (UUID uuid) {
        return Optional.empty();
    }

    /**
     * Creates a home
     * @return If the player was able to create a home
     */
    public boolean createHome (Player player) {
        return false;
    }

    /**
     * Creates required SQL tables
     */
    private void setupTables () {
        synchronized (plugin.getDatabase().getConnection()) {
            Statement stmt = null;
            try {
                stmt = plugin.getDatabase().getConnection().createStatement();
                stmt.execute(CREATE_HOMES_TABLES);
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

}
