package io.github.willqi.pizzamc.claims.plugin.listeners;

import io.github.willqi.pizzamc.claims.api.claims.ChunkCoordinates;
import io.github.willqi.pizzamc.claims.api.claims.ClaimsManager;
import io.github.willqi.pizzamc.claims.plugin.ClaimsPlugin;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import java.util.logging.Level;

public class ClaimListener implements Listener {

    private final ClaimsPlugin plugin;

    public ClaimListener(ClaimsPlugin plugin) {
        this.plugin = plugin;
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
        this.plugin.getClaimsManager().removeClaimFromCache(new ChunkCoordinates(event.getChunk().getWorld().getUID(), event.getChunk().getX(), event.getChunk().getZ()));
    }

    private void requestChunksAround(Location location) {
        Chunk chunk = location.getChunk();
        for (int x = chunk.getX() - 1; x <= chunk.getX() + 1; x++) {
            for (int z = chunk.getZ() - 1; z <= chunk.getZ() + 1; z++) {
                this.plugin.getClaimsManager().fetchClaim(new ChunkCoordinates(chunk.getWorld().getUID(), x, z)).whenComplete((claim, exception) -> {
                    if (exception != null) {
                        this.plugin.getLogger().log(Level.SEVERE, "An exception occurred while loading a claim chunk", exception);
                    }
                });
            }
        }
    }

}
