package io.github.willqi.pizzamc.claims.api.claims;

import io.github.willqi.pizzamc.claims.ClaimsPlugin;
import io.github.willqi.pizzamc.claims.api.claims.exceptions.NoClaimOwnerException;
import org.bukkit.Chunk;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
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

    // Global map for all chunk data.
    private final Map<ChunkCoordinates, Claim> claims = new ConcurrentHashMap<>();

    private final ClaimsPlugin plugin;

    private static int CLAIM_ID = 0;
    private static int CLAIM_HELPER_ID = 0;

    public ClaimsManager (final ClaimsPlugin plugin) {
        this.plugin = plugin;
        initSQLQueries();
    }

    /**
     * Retrieve claim data about a chunk
     * @param chunk
     * @return The claim data
     */
    public Claim getClaim (final Chunk chunk) {
        return getClaim(
                new ChunkCoordinates(chunk.getX(), chunk.getZ(), chunk.getWorld().getUID())
        );
    }

    /**
     * Retreive claim data from it's coordinates
     * @param coordinates
     * @return the claim data
     */
    public Claim getClaim (final ChunkCoordinates coordinates) {
        if (!claims.containsKey(coordinates)) {

            final Claim claim = new Claim(
                    Optional.empty(),
                    coordinates.getWorldUUID(),
                    coordinates.getX(),
                    coordinates.getZ(),
                    null,
                    0,
                    new ArrayList<>()
            );

            claims.put(coordinates, claim);

        }
        return claims.get(coordinates);
    }

    /**
     * Retrieve all the claims a player has
     * @param player
     * @return The claims they own that have been loaded in
     */
    public Map<ChunkCoordinates, Claim> getClaims (final OfflinePlayer player) {
        return getClaims(player.getUniqueId());
    }

    /**
     * Retrieve all the claims a uuid has
     * @param uuid
     * @return The claims they own that have been loaded in
     */
    public Map<ChunkCoordinates, Claim> getClaims (final UUID uuid) {
        final Map<ChunkCoordinates, Claim> playerClaims = new HashMap<>();
        for (final Map.Entry<ChunkCoordinates, Claim> entry : claims.entrySet()) {
            if (entry.getValue().getOwner().isPresent() && entry.getValue().getOwner().get().getUniqueId() == uuid) {
                playerClaims.put(entry.getKey(), entry.getValue());
            }
        }
        return playerClaims;
    }

    /**
     * Aadd a helper to a claim
     * @param player The player who is the helper
     * @param claim The claim
     * @return the helper object
     */
    public ClaimHelper addHelperToClaim (final OfflinePlayer player, final Claim claim) throws NoClaimOwnerException {
        if (!claim.getId().isPresent()) {
            throw new NoClaimOwnerException("Could not add a helper to a claim with no owner");
        }

        final ClaimHelper claimHelper = new ClaimHelper(
                getNewClaimHelperId(),
                claim.getId().get(),
                player,
                0,
                false
        );
        claim.addHelper(claimHelper);
        return claimHelper;
    }

    /**
     * Check if a player is allowed to claim more land
     * @param player
     * @return if they are
     */
    public boolean canClaimMoreLand (final Player player) {

        if (plugin.getConfig().getInt("max_claims_per_player") == -1) {
            return true;
        }

        return getClaims(player).values().size() < plugin.getConfig().getInt("max_claims_per_player");

    }

    /**
     * Called internally to save all unsaved data
     */
    public void cleanUp () {

        final Iterator<Claim> claimsIterator = claims.values().iterator();
        while (claimsIterator.hasNext()) {
            final Claim claim = claimsIterator.next();
            if (claim.isModified()) {
                claim.save();
            }
            for (final ClaimHelper helper : claim.getHelpers()) {
                if (helper.isModified()) {
                    helper.save();
                }
            }
            claimsIterator.remove();
        }

    }

    /**
     * Creates required SQL tables and gets CLAIM_ID and CLAIM_HELPER_ID
     */
    private void initSQLQueries() {
        synchronized (plugin.getDatabase().getConnection()) {
            Statement stmt = null;
            try {
                stmt = plugin.getDatabase().getConnection().createStatement();
                stmt.execute(CREATE_CLAIMS_TABLE);
                stmt.execute(CREATE_CLAIM_HELPERS_TABLE);
                final ResultSet claimsIdResult = stmt.executeQuery(SELECT_CLAIMS_ID);
                claimsIdResult.next(); // Impossible to be empty.
                CLAIM_ID = claimsIdResult.getInt("claim_id");
                final ResultSet helperIdResult = stmt.executeQuery(SELECT_CLAIM_HELPERS_ID);
                helperIdResult.next();
                CLAIM_HELPER_ID = helperIdResult.getInt("helper_id");

                final Map<Integer, List<ClaimHelper>> helpers = new HashMap<>();
                final ResultSet helpersQuery = stmt.executeQuery(SELECT_CLAIM_HELPERS);
                while (helpersQuery.next()) {

                    final int claimId = helpersQuery.getInt("claim_id");

                    final ClaimHelper helper = new ClaimHelper(
                            helpersQuery.getInt("id"),
                            claimId,
                            plugin.getServer().getOfflinePlayer(UUID.fromString(helpersQuery.getString("player"))),
                            helpersQuery.getInt("permissions"),
                            true
                    );

                    if (!helpers.containsKey(claimId)) {
                        helpers.put(claimId, new ArrayList<>());
                    }
                    helpers.get(claimId).add(helper);

                }

                final ResultSet claimsQuery = stmt.executeQuery(SELECT_CLAIMS);
                while (claimsQuery.next()) {
                    final Claim claim = new Claim(
                        Optional.of(claimsQuery.getInt("id")),
                        UUID.fromString(claimsQuery.getString("level")),
                        claimsQuery.getInt("x"),
                        claimsQuery.getInt("z"),
                        plugin.getServer().getOfflinePlayer(UUID.fromString(claimsQuery.getString("player"))),
                        claimsQuery.getInt("flags"),
                        helpers.getOrDefault(claimsQuery.getInt("id"), new ArrayList<>())
                    );
                    claims.put(new ChunkCoordinates(claimsQuery.getInt("x"), claimsQuery.getInt("z"), UUID.fromString(claimsQuery.getString("level"))), claim);
                }

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

    public static int getNewClaimId () {
        return CLAIM_ID++;
    }

    public static int getNewClaimHelperId () {
        return CLAIM_HELPER_ID++;
    }

    public static class ChunkCoordinates {

        private final int x;
        private final int z;
        private final UUID worldUUID;

        public ChunkCoordinates (final int x, final int z, final UUID worldUUID) {
            this.worldUUID = worldUUID;
            this.x = x;
            this.z = z;
        }

        public ChunkCoordinates (final Claim claim) {
            worldUUID = claim.getLevelUUID();
            x = claim.getX();
            z = claim.getZ();
        }

        public int getX () {
            return x;
        }

        public int getZ () {
            return z;
        }

        public UUID getWorldUUID () {
            return worldUUID;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof ChunkCoordinates) {
                ChunkCoordinates otherObj = (ChunkCoordinates)obj;
                return otherObj.getX() == x && otherObj.getZ() == z && otherObj.getWorldUUID() == worldUUID;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return x + z + worldUUID.hashCode();
        }
    }

}
