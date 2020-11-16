package io.github.willqi.pizzamc.claims.api;

import io.github.willqi.pizzamc.claims.ClaimsPlugin;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

/**
 * Used to interact with claims
 */
public class ClaimsManager {

    private static final String CREATE_CLAIMS_TABLE = "CREATE TABLE IF NOT EXISTS claims (" +
                                                        "id INT PRIMARY KEY," +
                                                        "x INT," +                             // Chunk X
                                                        "y INT," +                             // Chunk Y
                                                        "flags INT," +                         // Extra features for the chunk
                                                        "player VARCHAR(36)" +                   // UUID of the owner of the chunk
                                                        ")";
    private static final String CREATE_CHUNK_HELPERS_TABLES = "CREATE TABLE IF NOT EXISTS claim_helpers (" +
                                                                "id INT PRIMARY KEY," +
                                                                "x INT," +                      // Chunk X
                                                                "y INT," +                      // Chunk Y
                                                                "permissions INT," +             // Permissions the helper has.
                                                                "player VARCHAR(36)" +          // UUID of the owner of the chunk
                                                                ")";

    private static final String CREATE_HOMES_TABLES = "CREATE TABLE IF NOT EXISTS homes (" +
                                                        "id INT PRIMARY KEY," +
                                                        "x INT," +                              // x position
                                                        "y INT," +                              // y position
                                                        "z INT," +                              // z position
                                                        "name VARCHAR(50)," +                   // name of the home
                                                        "player VARCHAR(36)" +                  // UUID of the owner of the home
                                                        ")";

    private final ClaimsPlugin plugin;

    public ClaimsManager (ClaimsPlugin plugin) {
        this.plugin = plugin;
        setupTables();
    }

    private void setupTables () {
        synchronized (plugin.getDatabase().getConnection()) {
            Statement stmt = null;
            try {
                stmt = plugin.getDatabase().getConnection().createStatement();
                stmt.execute(CREATE_CLAIMS_TABLE);
                stmt.execute(CREATE_CHUNK_HELPERS_TABLES);
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
