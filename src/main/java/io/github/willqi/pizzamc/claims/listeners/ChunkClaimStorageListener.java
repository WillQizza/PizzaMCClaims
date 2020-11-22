package io.github.willqi.pizzamc.claims.listeners;

import io.github.willqi.pizzamc.claims.ClaimsPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

/**
 * Listener responsible for loading/unloading chunk data.
 */
public class ChunkClaimStorageListener implements Listener {

    private final ClaimsPlugin plugin;

    public ChunkClaimStorageListener (final ClaimsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChat (AsyncPlayerChatEvent event) {
        plugin.getClaimsManager().getClaim(event.getPlayer().getLocation().getChunk()).setOwner(event.getPlayer());
    }

}
