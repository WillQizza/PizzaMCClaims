package io.github.willqi.pizzamc.claims.plugin.listeners;

import io.github.willqi.pizzamc.claims.api.homes.HomesManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Listener responsible for everything related to homes.
 */
public class HomeListener implements Listener {

    private final HomesManager manager;

    public HomeListener (HomesManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void loadHomesOnPlayerJoin (PlayerJoinEvent event) {
        this.manager.loadHomes(event.getPlayer());
    }

    @EventHandler
    public void unloadHomesOnPlayerLeave (PlayerQuitEvent event) {
        this.manager.unloadHomes(event.getPlayer());
    }

}
