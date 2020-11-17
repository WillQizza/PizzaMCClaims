package io.github.willqi.pizzamc.claims.api.claims;

import io.github.willqi.pizzamc.claims.database.SaveableObject;
import org.bukkit.OfflinePlayer;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

public class Claim implements SaveableObject {

    public enum Flags {
        ALWAYS_DAY(generateValue(0)),       // Is it always day?
        MOB_SPAWNING(generateValue(1)),     // Can mobs spawn?
        PVP(generateValue(2)),              // Is PVP enabled?
        WHITELIST(generateValue(3));        // Can only select individuals enter?

        private int value;

        Flags (int value) {
            this.value = value;
        }

        public int getValue () {
            return value;
        }

        private static int generateValue (int index) {
            return (int)Math.pow(2, index);
        }
    }

    private final int x;
    private final int z;
    private final int id;

    private int flags = 0;
    private boolean claimed = false;
    private boolean wasModified = false;
    private OfflinePlayer owner = null;
    private List<ClaimHelper> helpers;

    /**
     * Default constructor for if no data is in the database.
     * @param id Claim id in the database.
     * @param x Chunk X
     * @param z Chunk Z
     */
    public Claim (int id, int x, int z) {
        this.id = id;
        this.x = x;
        this.z = z;
        this.helpers = new CopyOnWriteArrayList<>();
    }

    /**
     * Constructor for data from the database
     * @param id Claim id in the database.
     * @param x Chunk X
     * @param z Chunk Z
     * @param owner Owner of the chunk
     * @param flags Flags of the chunk
     * @param helpers Other players with permissions in the chunk
     */
    public Claim (int id, int x, int z, OfflinePlayer owner, int flags, List<ClaimHelper> helpers) {
        this.id = id;
        this.x = x;
        this.z = z;
        this.owner = owner;
        this.flags = flags;
        this.helpers = new CopyOnWriteArrayList<>(helpers);
    }

    public int getId () {
        return id;
    }

    public int getX () {
        return x;
    }

    public int getZ () {
        return z;
    }

    public Optional<OfflinePlayer> getOwner () {
        return Optional.empty();
    }

    public List<ClaimHelper> getHelpers () {
        return helpers;
    }

    public Claim setOwner (OfflinePlayer player) {
        owner = player;
        wasModified = true;
        return this;
    }

    public Claim setFlags (int flags) {
        this.flags = flags;
        return this;
    }

    public Claim addFlag (Flags flag) {
        if ((flags & flag.getValue()) == 0) {
            flags += flag.getValue();
            wasModified = true;
        }
        return this;
    }

    public Claim removeFlag (Flags flag) {
        if ((flags & flag.getValue()) != 0) {
            flags -= flag.getValue();
            wasModified = true;
        }
        return this;
    }

    public boolean hasFlag (Flags flag) {
        return (flags & flag.getValue()) != 0;
    }

    @Override
    public boolean isModified() {
        return wasModified;
    }

    @Override
    public void save() {

    }

}
