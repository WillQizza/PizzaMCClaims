package io.github.willqi.pizzamc.claims.api.homes;

import io.github.willqi.pizzamc.claims.ClaimsPlugin;
import io.github.willqi.pizzamc.claims.database.SaveableObject;
import org.bukkit.OfflinePlayer;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;

public class Home implements SaveableObject {

    private static final String SAVE_QUERY = "REPLACE INTO homes (id, level, x, y, z, name, player) VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String DELETE_QUERY = "DELETE FROM homes WHERE id=?";

    private final int id;
    private final int x;
    private final int y;
    private final int z;
    private final OfflinePlayer player;
    private final String name;
    private final UUID levelUUID;

    private boolean wasModified;
    private boolean destroyed;

    public Home (int id, OfflinePlayer player, UUID levelUUID, String name, int x, int y, int z, boolean fromDatabase) {
        this.id = id;
        this.player = player;
        this.levelUUID = levelUUID;
        this.name = name;
        this.x = x;
        this.y = y;
        this.z = z;
        wasModified = !fromDatabase;
    }


    /**
     * Get the UUID of the level this home is in
     * @return the UUID
     */
    public UUID getLevelUUID () {
        return levelUUID;
    }

    /**
     * Get the id of the home
     * @return the id of the home
     */
    public int getId () {
        return id;
    }

    /**
     * Get the x coordinate of a home
     * @return the x
     */
    public int getX () {
        return x;
    }

    /**
     * Get the y coordinate of a home
     * @return the y
     */
    public int getY () {
        return y;
    }

    /**
     * Get the z coordinate of a home
     * @return the z
     */
    public int getZ () {
        return z;
    }

    /**
     * Get the owner of the home
     * @return the owner
     */
    public OfflinePlayer getOwner () {
        return player;
    }

    /**
     * Get the name of the home
     * @return the name
     */
    public String getName () {
        return name;
    }

    public boolean isDestroyed () {
        return destroyed;
    }

    /**
     * Destroy the home.
     */
    public void destroy () {
        destroyed = true;
        wasModified = true;
    }

    @Override
    public boolean isModified() {
        return wasModified;
    }

    @Override
    public void save() {

        ClaimsPlugin plugin = ClaimsPlugin.getPlugin(ClaimsPlugin.class);
        synchronized (plugin.getDatabase().getConnection()) {
            PreparedStatement stmt = null;
            try {
                if (destroyed) {
                    stmt = plugin.getDatabase().getConnection().prepareStatement(DELETE_QUERY);
                } else {
                    stmt = plugin.getDatabase().getConnection().prepareStatement(SAVE_QUERY);
                    stmt.setString(2, levelUUID.toString());
                    stmt.setInt(3, x);
                    stmt.setInt(4, y);
                    stmt.setInt(5, z);
                    stmt.setString(6, name);
                    stmt.setString(7, player.getUniqueId().toString());
                }
                stmt.setInt(1, id);
                stmt.execute();
            } catch (SQLException exception) {
                plugin.getLogger().log(Level.WARNING, "Failed to save data for home id: " + id);
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
