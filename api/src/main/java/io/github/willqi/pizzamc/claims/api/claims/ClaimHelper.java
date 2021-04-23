package io.github.willqi.pizzamc.claims.api.claims;

import java.util.Objects;
import java.util.UUID;

public class ClaimHelper implements Cloneable {

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

    public void setPermissions (int permissions) {
        this.permissions = permissions;
    }

    /**
     * Add a permission to the helper
     * @param permission
     * @return the helper
     */
    public void addPermission (Permission permission) {
        if ((this.permissions & permission.getValue()) == 0) {
            this.permissions += permission.getValue();
        }
    }

    public void removePermission (Permission permission) {
        if ((this.permissions & permission.getValue()) != 0) {
            this.permissions -= permission.getValue();
        }
    }

    public boolean hasPermission (Permission permission) {
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

    public enum Permission {

        BUILD("Build", "Allow this player to build and destroy blocks!", generateValue(0)),
        INTERACT("Interact", "Allow this player to interact with blocks!", generateValue(1));

        private final String title;
        private final String description;
        private final int value;

        Permission(String title, String description, int value) {
            this.title = title;
            this.description = description;
            this.value = value;
        }

        public String getTitle() {
            return this.title;
        }

        public String getDescription() {
            return this.description;
        }

        public int getValue() {
            return this.value;
        }

        public static Permission getPermisionByValue(int value) {
            for (Permission permission : Permission.values()) {
                if (permission.getValue() == value) {
                    return permission;
                }
            }
            return null;
        }

        private static int generateValue(final int index) {
            return (int)Math.pow(2, index);
        }

    }

}
