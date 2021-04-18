package io.github.willqi.pizzamc.claims.plugin.listeners;

import io.github.willqi.pizzamc.claims.api.homes.HomesManager;
import io.github.willqi.pizzamc.claims.plugin.ClaimsPlugin;
import io.github.willqi.pizzamc.claims.plugin.Utility;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.logging.Level;

public class HomeListener implements Listener {

    private final ClaimsPlugin plugin;

    public HomeListener (ClaimsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void loadHomesOnPlayerJoin (PlayerJoinEvent event) {
        this.plugin.getHomesManager().fetchHomes(event.getPlayer().getUniqueId()).whenComplete((homes, exception) -> {
            if (exception != null) {
                this.plugin.getLogger().log(Level.WARNING, "An exception occurred while trying to load in the homes of " + event.getPlayer().getUniqueId(), exception);
                event.getPlayer().sendMessage(Utility.formatResponse("Homes", "An exception occurred while trying to load in your homes!", ChatColor.RED));
            }
        });
    }

    @EventHandler
    public void unloadHomesOnPlayerLeave (PlayerQuitEvent event) {
        this.plugin.getHomesManager().clearHomesCache(event.getPlayer().getUniqueId());
    }

}
