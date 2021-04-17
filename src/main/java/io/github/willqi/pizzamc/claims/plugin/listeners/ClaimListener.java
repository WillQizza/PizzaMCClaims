package io.github.willqi.pizzamc.claims.plugin.listeners;

import io.github.willqi.pizzamc.claims.api.claims.ChunkCoordinates;
import io.github.willqi.pizzamc.claims.api.claims.ClaimsManager;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

public class ClaimListener implements Listener {

    private final ClaimsManager claimsManager;

    public ClaimListener(ClaimsManager claimsManager) {
        this.claimsManager = claimsManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        this.requestChunksAround(event.getPlayer().getLocation());
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Chunk currentChunk = event.getTo().getChunk();
        Chunk previousChunk = event.getFrom().getChunk();
        if ((currentChunk.getX() != previousChunk.getX()) || (currentChunk.getZ() != previousChunk.getZ())) {
            this.requestChunksAround(event.getTo());
        }
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        this.claimsManager.removeClaimFromCache(new ChunkCoordinates(event.getChunk().getWorld().getUID(), event.getChunk().getX(), event.getChunk().getZ()));
    }

    private void requestChunksAround(Location location) {
        Chunk chunk = location.getChunk();
        for (int x = chunk.getX() - 1; x <= chunk.getX() + 1; x++) {
            for (int z = chunk.getZ() - 1; z <= chunk.getZ() + 1; z++) {
                this.claimsManager.fetchClaim(new ChunkCoordinates(chunk.getWorld().getUID(), x, z));
            }
        }
    }

}
