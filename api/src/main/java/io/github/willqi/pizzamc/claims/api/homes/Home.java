package io.github.willqi.pizzamc.claims.api.homes;

import io.github.willqi.pizzamc.claims.api.exceptions.InvalidHomeNameException;

import java.util.Objects;
import java.util.UUID;

public class Home implements Cloneable {

    public static final int MAX_NAME_LENGTH = 50;

    private final String name;
    private final UUID ownerUUID;

    private double x;
    private double y;
    private double z;
    private UUID worldUUID;

    public Home(UUID ownerUUID, String name, UUID worldUUID, double x, double y, double z) throws InvalidHomeNameException {
        this.ownerUUID = ownerUUID;
        this.name = name;
        this.worldUUID = worldUUID;
        this.x = x;
        this.y = y;
        this.z = z;

        if (this.name.length() > MAX_NAME_LENGTH) {
            throw new InvalidHomeNameException("The home name is too long");
        }
    }

    public UUID getWorldUUID() {
        return this.worldUUID;
    }
    public void setWorldUUID(UUID uuid) {
        this.worldUUID = uuid;
    }

    public double getX() {
        return this.x;
    }
    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return this.y;
    }
    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return this.z;
    }
    public void setZ(double z) {
        this.z = z;
    }

    public UUID getOwnerUUID() {
        return this.ownerUUID;
    }
    public String getName() {
        return this.name;
    }

    @Override
    public Home clone() {
        try {
            return (Home)super.clone();
        } catch (CloneNotSupportedException exception) {
            try {
                return new Home(this.ownerUUID, this.name, this.worldUUID, this.x, this.y, this.z);
            } catch (InvalidHomeNameException homeNameException) {
                throw new AssertionError("Failed to clone new home. Unexpectedly threw invalid home name");
            }
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.name, this.ownerUUID);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Home) {
            Home home = (Home)obj;
            return home.getName().equals(this.getName()) && home.getOwnerUUID().equals(this.getOwnerUUID());
        } else {
            return false;
        }
    }
}
