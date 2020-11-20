package io.github.willqi.pizzamc.claims.api.claims;

import io.github.willqi.pizzamc.claims.database.SaveableObject;
import org.bukkit.OfflinePlayer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class Claim implements SaveableObject {

    /**
     * Features of the claim
     */
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
    private final UUID levelUUID;

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
    public Claim (int id, UUID levelUUID, int x, int z) {
        this.id = id;
        this.levelUUID = levelUUID;
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
    public Claim (int id, UUID levelUUID, int x, int z, OfflinePlayer owner, int flags, List<ClaimHelper> helpers) {
        this.id = id;
        this.levelUUID = levelUUID;
        this.x = x;
        this.z = z;
        this.owner = owner;
        this.flags = flags;
        this.helpers = new CopyOnWriteArrayList<>(helpers);
    }

    /**
     * Retrieve the claim id
     * @return chunk id
     */
    public int getId () {
        return id;
    }

    /**
     * Retrieve the level UUID
     * @return level uuid
     */
    public UUID getLevelUUID () {
        return levelUUID;
    }

    /**
     * Retrieve the chunk x
     * @return chunk x
     */
    public int getX () {
        return x;
    }

    /**
     * Retrieve the chunk z
     * @return chunk z
     */
    public int getZ () {
        return z;
    }

    /**
     * Retrieve the owner of the claim
     * @return The owner if the chunk is claimed.
     */
    public Optional<OfflinePlayer> getOwner () {
        return Optional.empty();
    }

    /**
     * Retrieve all of the helpers of the claim
     * @return A list of helpers who have permissions in this claim
     */
    public List<ClaimHelper> getHelpers () {
        return helpers;
    }

    /**
     * Set the owner of the claim
     * @param player
     * @return the claim
     */
    public Claim setOwner (OfflinePlayer player) {
        owner = player;
        wasModified = true;
        return this;
    }

    /**
     * Set the claim flags
     * @param flags
     * @return the claim
     */
    public Claim setFlags (int flags) {
        this.flags = flags;
        return this;
    }

    /**
     * Add a flag to the claim
     * @param flag
     * @return the claim
     */
    public Claim addFlag (Flags flag) {
        if ((flags & flag.getValue()) == 0) {
            flags += flag.getValue();
            wasModified = true;
        }
        return this;
    }

    /**
     * Remove a flag from the claim
     * @param flag
     * @return the claim
     */
    public Claim removeFlag (Flags flag) {
        if ((flags & flag.getValue()) != 0) {
            flags -= flag.getValue();
            wasModified = true;
        }
        return this;
    }


    /**
     * Check if a claim has a flag
     * @param flag
     * @return if the claim has the flag
     */
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
