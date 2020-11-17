package io.github.willqi.pizzamc.claims.api.claims;

import io.github.willqi.pizzamc.claims.database.SaveableObject;
import org.bukkit.OfflinePlayer;

public class ClaimHelper implements SaveableObject {

    /**
     * Permissions for the helper.
     */
    public enum Permissions {

        ADMIN(generateValue(0)),
        WHITELISTED(generateValue(1)),
        FLY(generateValue(2)),
        BUILD(generateValue(3)),
        INTERACT(generateValue(4));

        private int value;

        Permissions (int value) {
            this.value = value;
        }

        public int getValue () {
            return value;
        }

        private static int generateValue (int index) {
            return (int)Math.pow(2, index);
        }

    }

    private final int id;
    private final OfflinePlayer player;

    private int permissions = 0;
    private boolean wasModified = false;

    /**
     * Constructor for claim helper
     * @param id
     * @param player
     * @param permissions
     */
    public ClaimHelper (int id, OfflinePlayer player, int permissions) {
        this.id = id;
        this.player = player;
        this.permissions = permissions;
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
    public ClaimHelper setPermissions (int permissions) {
        this.permissions = permissions;
        wasModified = true;
        return this;
    }

    /**
     * Add a permission to the helper
     * @param permission
     * @return the helper
     */
    public ClaimHelper addPermission (Permissions permission) {
        if ((permissions & permission.getValue()) == 0) {
            permissions += permission.getValue();
            wasModified = true;
        }
        return this;
    }

    /**
     * Remove a permission to the helper
     * @param permission
     * @return the helper
     */
    public ClaimHelper removePermission (Permissions permission) {
        if ((permissions & permission.getValue()) != 0) {
            permissions -= permission.getValue();
            wasModified = true;
        }
        return this;
    }

    /**
     * Check if a helper has a permission
     * @param permission
     * @return If the helper has the permission
     */
    public boolean hasPermission (Permissions permission) {
        return (permissions & permission.getValue()) != 0;
    }


    @Override
    public boolean isModified() {
        return wasModified;
    }

    @Override
    public void save() {

    }
}
