package io.github.willqi.pizzamc.claims.api.claims;

import io.github.willqi.pizzamc.claims.ClaimsPlugin;
import org.bukkit.Chunk;
import org.bukkit.OfflinePlayer;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Used to interact with claims
 */
public class ClaimsManager {

    private static final String CREATE_CLAIMS_TABLE = "CREATE TABLE IF NOT EXISTS claims (" +
                                                        "id INT PRIMARY KEY," +
                                                        "level VARCHAR(36) ," +                // UUID of the level
                                                        "x INT," +                             // Chunk X
                                                        "y INT," +                             // Chunk Y
                                                        "flags INT," +                         // Extra features for the chunk
                                                        "player VARCHAR(36)" +                 // UUID of the owner of the chunk
                                                        ")";
    private static final String CREATE_CHUNK_HELPERS_TABLES = "CREATE TABLE IF NOT EXISTS claim_helpers (" +
                                                        "claim_id INT," +               // Corresponding claim id
                                                        "permissions INT," +            // Permissions the helper has.
                                                        "player VARCHAR(36)" +          // UUID of the owner of the chunk
                                                        ")";

    private final ClaimsPlugin plugin;

    private int CLAIM_ID = 0;
    private int CLAIM_HELPER_ID = 0;

    private Map<String, Claim> claims = new ConcurrentHashMap<>();

    public ClaimsManager (ClaimsPlugin plugin) {
        this.plugin = plugin;
        setupTables();
    }

    /**
     * Check if a chunk has it's claim data retrieved from the database.
     * @param chunk
     * @return If the chunk's claim data has been loaded from the database.
     */
    public boolean isClaimLoaded (Chunk chunk) {
        return false;
    }

    /**
     * Retrieve claim data about a chunk
     * @param chunk
     * @return The claim data
     */
    public Optional<Claim> getClaim (Chunk chunk) {
        return Optional.empty();
    }

    /**
     * Retrieve all the claims a user has
     * @param player
     * @return The claims they own that have been loaded in
     */
    public Optional<List<Claim>> getClaims (OfflinePlayer player) {
        return getClaims(player.getUniqueId());
    }

    /**
     * Retrieve all the claims a user has
     * @param uuid
     * @return The claims they own that have been loaded in
     */
    public Optional<List<Claim>> getClaims (UUID uuid) {
        return Optional.empty();
    }

    /**
     * Load all claims owned by a player
     * @param player
     */
    public void loadClaims (OfflinePlayer player) {
        loadClaims(player.getUniqueId());
    }

    /**
     * Load all claims owns by a player
     * @param uuid
     */
    public void loadClaims (UUID uuid) {

    }

    /**
     * Load a claim from a chunk
     * @param chunk
     */
    public void loadClaim (Chunk chunk) {

    }

    /**
     * Unload a chunk and save any changes to the database.
     * @param chunk
     */
    public void unloadClaim (Chunk chunk) {

    }

    /**
     * Creates required SQL tables
     */
    private void setupTables () {
        synchronized (plugin.getDatabase().getConnection()) {
            Statement stmt = null;
            try {
                stmt = plugin.getDatabase().getConnection().createStatement();
                stmt.execute(CREATE_CLAIMS_TABLE);
                stmt.execute(CREATE_CHUNK_HELPERS_TABLES);
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
