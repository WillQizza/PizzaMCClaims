package io.github.willqi.pizzamc.claims.api.claims;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class Claim implements Cloneable {

    private ChunkCoordinates coordinates;

    private int flags;
    private UUID owner = null;

    public Claim (ChunkCoordinates coordinates, int flags) {
        this.coordinates = coordinates;
        this.flags = flags;
    }

    public Claim (ChunkCoordinates coordinates, UUID owner, int flags) {
        this.coordinates = coordinates;
        this.owner = owner;
        this.flags = flags;
    }

    public ChunkCoordinates getCoordinates() {
        return this.coordinates;
    }

    /**
     * If nobody has claimed this chunk, there is no owner
     * @return
     */
    public Optional<UUID> getOwner () {
        return Optional.ofNullable(owner);
    }

    public void setOwner (UUID uuid) {
        this.owner = uuid;
    }

    public int getFlags() {
        return this.flags;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }

    public void addFlag(Flags flag) {
        if ((this.flags & flag.getValue()) == 0) {
            this.flags += flag.getValue();
        }
    }

    public void removeFlag(Flags flag) {
        if ((this.flags & flag.getValue()) != 0) {
            this.flags -= flag.getValue();
        }
    }

    public boolean hasFlag(Flags flag) {
        return (this.flags & flag.getValue()) != 0;
    }

    @Override
    protected Claim clone() {
        try {
            return (Claim)super.clone();
        } catch (CloneNotSupportedException exception) {
            if (this.getOwner().isPresent()) {
                return new Claim(this.coordinates, this.owner, this.getFlags());
            } else {
                return new Claim(this.coordinates, this.getFlags());
            }
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.coordinates);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Claim) {
            Claim claimObj = (Claim)obj;
            return claimObj.coordinates.equals(this.coordinates);
        } else {
            return false;
        }
    }

    public enum Flags {
        ALWAYS_DAY(generateValue(0)),       // Is it always day?
        MOB_SPAWNING(generateValue(1)),     // Can mobs spawn?
        PVP(generateValue(2)),              // Is PVP enabled?
        WHITELIST(generateValue(3));        // Can only select individuals enter?

        private final int value;

        Flags(final int value) {
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
