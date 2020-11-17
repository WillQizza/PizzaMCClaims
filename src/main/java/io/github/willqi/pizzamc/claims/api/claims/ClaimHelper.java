package io.github.willqi.pizzamc.claims.api.claims;

import io.github.willqi.pizzamc.claims.database.SaveableObject;
import org.bukkit.OfflinePlayer;

public class ClaimHelper implements SaveableObject {

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

    public ClaimHelper (int id, OfflinePlayer player, int permissions) {
        this.id = id;
        this.player = player;
        this.permissions = permissions;
    }

    public OfflinePlayer getPlayer () {
        return player;
    }

    public ClaimHelper setPermissions (int permissions) {
        this.permissions = permissions;
        wasModified = true;
        return this;
    }

    public ClaimHelper addPermission (Permissions permission) {
        if ((permissions & permission.getValue()) == 0) {
            permissions += permission.getValue();
            wasModified = true;
        }
        return this;
    }

    public ClaimHelper removePermission (Permissions permission) {
        if ((permissions & permission.getValue()) != 0) {
            permissions -= permission.getValue();
            wasModified = true;
        }
        return this;
    }

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
