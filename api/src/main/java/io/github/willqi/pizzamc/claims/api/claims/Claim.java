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

    public void addFlag(Flag flag) {
        if ((this.flags & flag.getValue()) == 0) {
            this.flags += flag.getValue();
        }
    }

    public void removeFlag(Flag flag) {
        if ((this.flags & flag.getValue()) != 0) {
            this.flags -= flag.getValue();
        }
    }

    public boolean hasFlag(Flag flag) {
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

    /**
     * Special modifiers for the claim
     */
    public enum Flag {
        ALWAYS_DAY("Always Day", "Keep your claim always sunny!", generateValue(0)),       // Is it always day?
        DISABLE_MOB_SPAWNING("Disable Mob Spawning", "Disable mobs from spawning in your claim!", generateValue(1)),     // Can mobs spawn?
        DENY_PVP("No PVP", "Prevent players from attacking each other in your claim!", generateValue(2));              // Is PVP enabled?

        private final String title;
        private final String description;
        private final int value;

        Flag(String title, String description, int value) {
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

        private static int generateValue(final int index) {
            return (int)Math.pow(2, index);
        }

        public static Flag getFlagByValue(int value) {
            for (Flag flag : Flag.values()) {
                if (flag.getValue() == value) {
                    return flag;
                }
            }
            return null;
        }
    }

}
