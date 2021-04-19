package io.github.willqi.pizzamc.claims.api.claims;

import java.util.Objects;
import java.util.UUID;

public class ChunkCoordinates {

    private final int x;
    private final int z;
    private final UUID worldUuid;

    public ChunkCoordinates(UUID worldUuid, int x, int z) {
        this.worldUuid = worldUuid;
        this.x = x;
        this.z = z;
    }

    public static ChunkCoordinates fromClaim(Claim claim) {
        return new ChunkCoordinates(claim.getWorldUuid(), claim.getX(), claim.getZ());
    }

    public int getX() {
        return this.x;
    }

    public int getZ() {
        return this.z;
    }

    public UUID getWorldUuid() {
        return this.worldUuid;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ChunkCoordinates) {
            ChunkCoordinates otherObj = (ChunkCoordinates) obj;
            return otherObj.getX() == this.getX() && otherObj.getZ() == this.getZ() && otherObj.getWorldUuid().equals(this.getWorldUuid());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.x, this.z, this.worldUuid);
    }
}
