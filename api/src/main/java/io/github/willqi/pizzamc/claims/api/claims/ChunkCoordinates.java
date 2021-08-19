package io.github.willqi.pizzamc.claims.api.claims;

import java.util.Objects;
import java.util.UUID;

public class ChunkCoordinates {

    private final int x;
    private final int z;
    private final UUID worldUUID;

    public ChunkCoordinates(UUID worldUUID, int x, int z) {
        this.worldUUID = worldUUID;
        this.x = x;
        this.z = z;
    }

    public int getX() {
        return this.x;
    }

    public int getZ() {
        return this.z;
    }

    public UUID getWorldUUID() {
        return this.worldUUID;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ChunkCoordinates) {
            ChunkCoordinates otherObj = (ChunkCoordinates) obj;
            return otherObj.getX() == this.getX() && otherObj.getZ() == this.getZ() && otherObj.getWorldUUID().equals(this.getWorldUUID());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.x, this.z, this.worldUUID);
    }
}
