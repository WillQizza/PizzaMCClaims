package io.github.willqi.pizzamc.claims.listeners;

import io.github.willqi.pizzamc.claims.ClaimsPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Listener responsible for everything related to homes.
 */
public class HomeListener implements Listener {

    private final ClaimsPlugin plugin;

    public HomeListener (final ClaimsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void loadHomesOnPlayerJoin (final PlayerJoinEvent event) {
        plugin.getHomesManager().loadHomes(event.getPlayer());
    }

    @EventHandler
    public void unloadHomesOnPlayerLeave (final PlayerQuitEvent event) {
        plugin.getHomesManager().unloadHomes(event.getPlayer());
    }

}
