package io.github.willqi.pizzamc.claims.api.homes;

import io.github.willqi.pizzamc.claims.database.SaveableObject;
import org.bukkit.OfflinePlayer;

public class Home implements SaveableObject {


    private final int x;
    private final int y;
    private final int z;
    private final OfflinePlayer player;
    private final String name;

    private boolean wasModified;
    private boolean destroyed;

    public Home (OfflinePlayer player, String name, int x, int y, int z) {
        this.player = player;
        this.name = name;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Get the x coordinate of a home
     * @return
     */
    public int getX () {
        return x;
    }

    /**
     * Get the y coordinate of a home
     * @return
     */
    public int getY () {
        return y;
    }

    /**
     * Get the z coordinate of a home
     * @return
     */
    public int getZ () {
        return z;
    }

    /**
     * Get the owner of the home
     * @return
     */
    public OfflinePlayer getOwner () {
        return player;
    }

    /**
     * Get the name of the home
     * @return
     */
    public String getName () {
        return name;
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

    }

}
