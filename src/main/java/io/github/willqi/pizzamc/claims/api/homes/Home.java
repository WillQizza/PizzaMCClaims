package io.github.willqi.pizzamc.claims.api.homes;

import io.github.willqi.pizzamc.claims.api.exceptions.InvalidHomeNameException;

import java.util.Objects;
import java.util.UUID;

public class Home implements Cloneable {

    public static final int MAX_NAME_LENGTH = 50;

    private final String name;
    private final UUID ownerUuid;

    private double x;
    private double y;
    private double z;
    private UUID worldUuid;

    public Home(UUID ownerUuid, String name, UUID worldUuid, double x, double y, double z) throws InvalidHomeNameException {
        this.ownerUuid = ownerUuid;
        this.name = name;
        this.worldUuid = worldUuid;
        this.x = x;
        this.y = y;
        this.z = z;

        if (this.name.length() > MAX_NAME_LENGTH) {
            throw new InvalidHomeNameException("The home name is too long");
        }
    }

    public UUID getWorldUuid() {
        return worldUuid;
    }
    public void setWorldUuid(UUID uuid) {
        this.worldUuid = uuid;
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

    public UUID getOwnerUuid() {
        return this.ownerUuid;
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
                return new Home(this.ownerUuid, this.name, this.worldUuid, this.x, this.y, this.z);
            } catch (InvalidHomeNameException homeNameException) {
                throw new AssertionError("Failed to clone new home. Unexpectedly threw invalid home name");
            }
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.name, this.ownerUuid);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Home) {
            Home home = (Home)obj;
            return home.getName().equals(this.getName()) && home.getOwnerUuid().equals(this.getOwnerUuid());
        } else {
            return false;
        }
    }
}
