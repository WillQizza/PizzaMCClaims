package io.github.willqi.pizzamc.claims.api.claims;

import java.util.Objects;
import java.util.UUID;

public class ClaimHelper implements Cloneable {

    private static final String SAVE_QUERY = "REPLACE INTO claim_helpers (id, claim_id, permissions, player) VALUES (?, ?, ?, ?)";
    private static final String DELETE_QUERY = "DELETE FROM claim_helpers WHERE id=?";

    private int permissions;
    private UUID uuid;

    public ClaimHelper(UUID helperUuid, int permissions) {
        this.uuid = helperUuid;
        this.permissions = permissions;
    }

    public ClaimHelper(UUID helperUuid) {
        this.uuid = helperUuid;
    }

    public int getPermissions() {
        return this.permissions;
    }

    public UUID getUuid() {
        return this.uuid;
    }

    public ClaimHelper setPermissions (int permissions) {
        this.permissions = permissions;
        return this;
    }

    /**
     * Add a permission to the helper
     * @param permission
     * @return the helper
     */
    public ClaimHelper addPermission (Permissions permission) {
        if ((this.permissions & permission.getValue()) == 0) {
            this.permissions += permission.getValue();
        }
        return this;
    }

    public ClaimHelper removePermission (Permissions permission) {
        if ((this.permissions & permission.getValue()) != 0) {
            this.permissions -= permission.getValue();
        }
        return this;
    }

    public boolean hasPermission (Permissions permission) {
        return (this.permissions & permission.getValue()) != 0;
    }

    @Override
    protected ClaimHelper clone() {
        try {
            return (ClaimHelper)super.clone();
        } catch (CloneNotSupportedException exception) {
            return new ClaimHelper(this.getUuid(), this.getPermissions());
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.uuid);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ClaimHelper) {
            ClaimHelper helper = (ClaimHelper)obj;
            return helper.getUuid().equals(this.getUuid());
        } else {
            return false;
        }
    }

    public enum Permissions {

        ADMIN(generateValue(0)),
        FLY(generateValue(1)),
        BUILD(generateValue(2)),
        INTERACT(generateValue(3));

        private final int value;

        Permissions(final int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        private static int generateValue(final int index) {
            return (int)Math.pow(2, index);
        }

    }

}
