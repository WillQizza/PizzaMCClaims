package io.github.willqi.pizzamc.claims.api.claims;

import io.github.willqi.pizzamc.claims.ClaimsPlugin;
import io.github.willqi.pizzamc.claims.database.SaveableObject;
import org.bukkit.OfflinePlayer;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

public class Claim implements SaveableObject {

    private final String SAVE_QUERY = "REPLACE INTO claims (id, level, x, z, flags, player) VALUES (?, ?, ?, ?, ?, ?)";
    private final String DELETE_QUERY = "DELETE FROM claims WHERE id=?";

    /**
     * Features of the claim
     */
    public enum Flags {
        ALWAYS_DAY(generateValue(0)),       // Is it always day?
        MOB_SPAWNING(generateValue(1)),     // Can mobs spawn?
        PVP(generateValue(2)),              // Is PVP enabled?
        WHITELIST(generateValue(3));        // Can only select individuals enter?

        private final int value;

        Flags (final int value) {
            this.value = value;
        }

        public int getValue () {
            return value;
        }

        private static int generateValue (final int index) {
            return (int)Math.pow(2, index);
        }
    }

    private final int x;
    private final int z;
    private final UUID levelUUID;
    private final List<ClaimHelper> helpers;
    private final AtomicInteger flags;

    private boolean claimed = false;
    private boolean wasModified = false;
    private Optional<Integer> id;
    private OfflinePlayer owner = null;

    /**
     * Default constructor for if no data is in the database.
     * @param id Claim id in the database.
     * @param levelUUID The level UUID
     * @param x Chunk X
     * @param z Chunk Z
     */
    public Claim (final Optional<Integer> id, final UUID levelUUID, final int x, final int z) {
        this.id = id;
        this.levelUUID = levelUUID;
        this.x = x;
        this.z = z;
        this.helpers = new CopyOnWriteArrayList<>();
        this.flags = new AtomicInteger(0);
    }

    /**
     * Constructor for data from the database
     * @param id Claim id in the database.
     * @param levelUUID UUID of the level
     * @param x Chunk X
     * @param z Chunk Z
     * @param owner Owner of the chunk
     * @param flags Flags of the chunk
     * @param helpers Other players with permissions in the chunk
     */
    public Claim (final Optional<Integer> id, final UUID levelUUID, final int x, final int z, final OfflinePlayer owner, final int flags, final List<ClaimHelper> helpers) {
        this.id = id;
        this.levelUUID = levelUUID;
        this.x = x;
        this.z = z;
        this.owner = owner;
        this.flags = new AtomicInteger(flags);
        this.helpers = new CopyOnWriteArrayList<>(helpers);
    }

    /**
     * Retrieve the claim id
     * @return chunk id
     */
    public Optional<Integer> getId () {
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
        return Optional.ofNullable(owner);
    }

    /**
     * Retrieve all of the helpers of the claim
     * @return A list of helpers who have permissions in this claim
     */
    public List<ClaimHelper> getHelpers () {
        return helpers;
    }

    /**
     * Add a helper to a claim
     * Should call ClaimsManager#addHelperToClaim to add a helper to a claim.
     * @param helper
     * @return the claim
     */
    public Claim addHelper (final ClaimHelper helper) {
        helpers.add(helper);
        return this;
    }

    /**
     * Set the owner of the claim
     * SHOULD use ClaimsManager#
     * @param player
     * @return the claim
     */
    public Claim setOwner (final OfflinePlayer player) {
        owner = player;
        wasModified = true;
        if (!id.isPresent() && player != null) {
            id = Optional.of(ClaimsManager.getNewClaimId());
        }
        if (player == null) {
            setFlags(0);
            for (final ClaimHelper helper : helpers) {
                helper.destroy();
            }
        }
        return this;
    }

    /**
     * Set the claim flags
     * @param flags
     * @return the claim
     */
    public Claim setFlags (final int flags) {
        this.flags.set(flags);
        wasModified = true;
        return this;
    }

    /**
     * Add a flag to the claim
     * @param flag
     * @return the claim
     */
    public Claim addFlag (final Flags flag) {
        final int currentVal = flags.get();
        if ((currentVal & flag.getValue()) == 0) {
            flags.compareAndSet(currentVal, currentVal + flag.getValue());
            wasModified = true;
        }
        return this;
    }

    /**
     * Remove a flag from the claim
     * @param flag
     * @return the claim
     */
    public Claim removeFlag (final Flags flag) {
        final int currentVal = flags.get();
        if ((currentVal & flag.getValue()) != 0) {
            flags.compareAndSet(currentVal, currentVal - flag.getValue());
            wasModified = true;
        }
        return this;
    }


    /**
     * Check if a claim has a flag
     * @param flag
     * @return if the claim has the flag
     */
    public boolean hasFlag (final Flags flag) {
        return (flags.get() & flag.getValue()) != 0;
    }

    @Override
    public boolean isModified() {
        return wasModified;
    }

    @Override
    public void save() {

        final ClaimsPlugin plugin = ClaimsPlugin.getPlugin(ClaimsPlugin.class);
        synchronized (plugin.getDatabase().getConnection()) {
            PreparedStatement stmt = null;
            try {
                if (getOwner().isPresent()) {
                    stmt = plugin.getDatabase().getConnection().prepareStatement(SAVE_QUERY);
                    stmt.setInt(1, id.get());
                    stmt.setString(2, levelUUID.toString());
                    stmt.setInt(3, x);
                    stmt.setInt(4, z);
                    stmt.setInt(5, flags.get());
                    if (owner == null) {
                        stmt.setString(6, null);
                    } else {
                        stmt.setString(6, owner.getUniqueId().toString());
                    }
                } else if (id.isPresent()) {
                    stmt = plugin.getDatabase().getConnection().prepareStatement(DELETE_QUERY);
                    stmt.setInt(1, id.get());
                }
                stmt.execute();
            } catch (SQLException exception) {
                plugin.getLogger().log(
                        Level.WARNING,
                        String.format("Failed to save claim (%s, %s) in dimension %s.", x, z, levelUUID)
                );
            } finally {
                if (stmt != null) {
                    try {
                        stmt.close();
                    } catch (SQLException exception) {}
                }
            }
        }

    }

}
