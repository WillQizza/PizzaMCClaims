package io.github.willqi.pizzamc.claims.api.claims;

import io.github.willqi.pizzamc.claims.ClaimsPlugin;
import io.github.willqi.pizzamc.claims.database.SaveableObject;
import org.bukkit.OfflinePlayer;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

public class ClaimHelper implements SaveableObject {

    private static final String SAVE_QUERY = "REPLACE INTO claim_helpers (id, claim_id, permissions, player) VALUES (?, ?, ?, ?)";
    private static final String DELETE_QUERY = "DELETE FROM claim_helpers WHERE id=?";

    /**
     * Permissions for the helper.
     */
    public enum Permissions {

        ADMIN(generateValue(0)),
        WHITELISTED(generateValue(1)),
        FLY(generateValue(2)),
        BUILD(generateValue(3)),
        INTERACT(generateValue(4));

        private final int value;

        Permissions (final int value) {
            this.value = value;
        }

        public int getValue () {
            return value;
        }

        private static int generateValue (final int index) {
            return (int)Math.pow(2, index);
        }

    }

    private final int id;
    private final int claimId;
    private final OfflinePlayer player;

    private AtomicInteger permissions = new AtomicInteger(0);
    private boolean wasModified = false;
    private boolean destroyed = false;

    /**
     * Constructor for claim helper
     * @param id
     * @param claimId
     * @param player
     * @param permissions
     */
    public ClaimHelper (final int id, final int claimId, final OfflinePlayer player, final int permissions, final boolean fromDatabase) {
        this.id = id;
        this.claimId = claimId;
        this.player = player;
        this.permissions = new AtomicInteger(permissions);
        this.wasModified = !fromDatabase;
    }

    /**
     * Get the claim id
     * @return the id of the claim
     */
    public int getClaimId () {
        return claimId;
    }

    /**
     * Get the claim helper player
     * @return the player equiv of the helper
     */
    public OfflinePlayer getPlayer () {
        return player;
    }

    /**
     * Set the permissions for the helper
     * @param permissions
     * @return the helper
     */
    public ClaimHelper setPermissions (final int permissions) {
        final int ogVal = this.permissions.get();
        this.permissions.compareAndSet(ogVal, ogVal + permissions);
        wasModified = true;
        return this;
    }

    /**
     * Add a permission to the helper
     * @param permission
     * @return the helper
     */
    public ClaimHelper addPermission (final Permissions permission) {
        final int ogVal = this.permissions.get();
        if ((permissions.get() & permission.getValue()) == 0) {
            permissions.compareAndSet(ogVal, ogVal + permission.getValue());
            wasModified = true;
        }
        return this;
    }

    /**
     * Remove a permission to the helper
     * @param permission
     * @return the helper
     */
    public ClaimHelper removePermission (final Permissions permission) {
        final int ogVal = permissions.get();
        if ((permissions.get() & permission.getValue()) != 0) {
            permissions.compareAndSet(ogVal, ogVal - permission.getValue());
            wasModified = true;
        }
        return this;
    }

    /**
     * Check if a helper has a permission
     * @param permission
     * @return If the helper has the permission
     */
    public boolean hasPermission (final Permissions permission) {
        return (permissions.get() & permission.getValue()) != 0;
    }


    public void destroy () {
        destroyed = true;
        permissions.set(0);
        wasModified = true;
    }

    @Override
    public boolean isModified() {
        return wasModified;
    }

    @Override
    public void save() {

        final ClaimsPlugin plugin = ClaimsPlugin.getPlugin(ClaimsPlugin.class);
        synchronized (plugin.getDatabase().getConnection()) {
            PreparedStatement stmt = null;
            try {
                if (destroyed || permissions.get() == 0) {
                    stmt = plugin.getDatabase().getConnection().prepareStatement(DELETE_QUERY);
                    stmt.setInt(1, id);
                } else {
                    stmt = plugin.getDatabase().getConnection().prepareStatement(SAVE_QUERY);
                    stmt.setInt(1, id);
                    stmt.setInt(2, claimId);
                    stmt.setInt(3, permissions.get());
                    stmt.setString(4, player.getUniqueId().toString());
                }
                stmt.execute();
            } catch (SQLException exception) {
                plugin.getLogger().log(Level.WARNING, "Failed to save claim helper data id: " + id);
            } finally {
                if (stmt != null) {
                    try {
                        stmt.close();
                    } catch (SQLException exception) {}
                }
            }

        }
    }
}
