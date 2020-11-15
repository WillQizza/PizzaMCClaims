package io.github.willqi.pizzamc.claims.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

/**
 * Listener responsible for loading/unloading chunk data.
 */
public class ChunkClaimStorageListener implements Listener {

    @EventHandler
    public void onChunkLoad (ChunkLoadEvent event) {
        // Load chunk from database.
    }

    @EventHandler
    public void onChunkUnload (ChunkUnloadEvent event) {
        // Unload chunk data from memory and save changes.
    }

}
