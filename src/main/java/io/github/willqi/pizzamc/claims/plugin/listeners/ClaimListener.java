package io.github.willqi.pizzamc.claims.plugin.listeners;

import io.github.willqi.pizzamc.claims.api.claims.ChunkCoordinates;
import io.github.willqi.pizzamc.claims.api.claims.Claim;
import io.github.willqi.pizzamc.claims.api.claims.ClaimHelper;
import io.github.willqi.pizzamc.claims.plugin.ClaimsPlugin;
import io.github.willqi.pizzamc.claims.plugin.Permissions;
import io.github.willqi.pizzamc.claims.plugin.Utility;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.world.ChunkUnloadEvent;

import java.util.Optional;
import java.util.logging.Level;

public class ClaimListener implements Listener {

    private final ClaimsPlugin plugin;

    public ClaimListener(ClaimsPlugin plugin) {
        this.plugin = plugin;
    }

    //
    //  DATA LOADING EVENT LISTENERS
    //

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        this.requestChunksAround(event.getPlayer().getLocation());
        this.plugin.getClaimsManager().fetchClaimCount(event.getPlayer().getUniqueId()).whenComplete((count, exception) -> {
            if (exception != null) {
                this.plugin.getLogger().log(Level.SEVERE, "Failed to fetch claim count of uuid " + event.getPlayer().getUniqueId(), exception);
            }
        });
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        this.plugin.getClaimsManager().removeClaimCountFromCache(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        this.handlePlayerMovementEvent(event);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        this.handlePlayerMovementEvent(event);
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        this.plugin.getClaimsManager().removeClaimFromCache(new ChunkCoordinates(event.getChunk().getWorld().getUID(), event.getChunk().getX(), event.getChunk().getZ()));
    }

    private void handlePlayerMovementEvent(PlayerMoveEvent event) {
        Chunk currentChunk = event.getTo().getChunk();
        Chunk previousChunk = event.getFrom().getChunk();
        if ((currentChunk.getX() != previousChunk.getX()) || (currentChunk.getZ() != previousChunk.getZ())) {
            this.requestChunksAround(event.getTo());
        }
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


    //
    // CLAIM FLAGS EVENT LISTENERS
    //

    @EventHandler
    public void onPlayerEnterClaim(PlayerMoveEvent event) {
        // Handle ALWAYS_DAY flag
        Chunk currentChunk = event.getTo().getChunk();
        Chunk previousChunk = event.getFrom().getChunk();
        if ((currentChunk.getX() != previousChunk.getX()) || (currentChunk.getZ() != previousChunk.getZ())) {

            ChunkCoordinates currentCoordinates = new ChunkCoordinates(
                    currentChunk.getWorld().getUID(),
                    currentChunk.getX(),
                    currentChunk.getZ()
            );
            Optional<Claim> currentChunkClaim = this.plugin.getClaimsManager().getClaim(currentCoordinates);
            if (currentChunkClaim.isPresent() && currentChunkClaim.get().hasFlag(Claim.Flag.ALWAYS_DAY)) {
                event.getPlayer().setPlayerTime(1000, false);
            } else {
                event.getPlayer().resetPlayerTime();
            }

        }
    }

    @EventHandler
    public void onMobSpawn(EntitySpawnEvent event) {
        if ((!(event.getEntity() instanceof Player)) && (event.getEntity() instanceof LivingEntity)) {
            ChunkCoordinates coordinates = new ChunkCoordinates(
                    event.getLocation().getWorld().getUID(),
                    event.getLocation().getChunk().getX(),
                    event.getLocation().getChunk().getZ()
            );
            Optional<Claim> claim = this.plugin.getClaimsManager().getClaim(coordinates);
            if (claim.isPresent() && claim.get().hasFlag(Claim.Flag.DISABLE_MOB_SPAWNING)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPVP(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
            ChunkCoordinates coordinates = new ChunkCoordinates(
                    event.getEntity().getWorld().getUID(),
                    event.getEntity().getLocation().getChunk().getX(),
                    event.getEntity().getLocation().getChunk().getZ()
            );
            Optional<Claim> claim = this.plugin.getClaimsManager().getClaim(coordinates);
            if (claim.isPresent() && claim.get().hasFlag(Claim.Flag.DENY_PVP)) {
                event.getDamager().sendMessage(Utility.formatResponse("Claims", "This is a no PVP zone!", ChatColor.RED));
                event.setCancelled(true);
            }
        }
    }


    //
    //  BUILD AND INTERACT EVENT LISTENERS
    //

    @EventHandler
    public void onClaimBlockPlace(BlockPlaceEvent event) {
        switch (this.getPlayerBuildStateInChunk(event.getPlayer(), event.getBlock().getChunk())) {
            case DENIED:
                event.setCancelled(true);
                event.getPlayer().sendMessage(Utility.formatResponse("Claims", "You do not have permission to build in this chunk!", ChatColor.RED));
                break;
            case LOADING:
                event.getPlayer().sendMessage(Utility.formatResponse("Claims", "Please wait a moment...", ChatColor.RED));
                event.setCancelled(true);
                break;
        }
    }

    @EventHandler
    public void onClaimBlockBreakEvent(BlockBreakEvent event) {
        switch (this.getPlayerBuildStateInChunk(event.getPlayer(), event.getBlock().getChunk())) {
            case DENIED:
                event.setCancelled(true);
                event.getPlayer().sendMessage(Utility.formatResponse("Claims", "You do not have permission to break blocks in this chunk!", ChatColor.RED));
                break;
            case LOADING:
                event.getPlayer().sendMessage(Utility.formatResponse("Claims", "Please wait a moment...", ChatColor.RED));
                event.setCancelled(true);
                break;
        }
    }

    @EventHandler
    public void onClaimInteraction(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            switch (event.getClickedBlock().getType()) {
                case ANVIL:
                case BED:
                case BREWING_STAND:
                case STONE_BUTTON:
                case WOOD_BUTTON:
                case CAULDRON:
                case CHEST:
                case COMMAND:
                case COMMAND_CHAIN:
                case COMMAND_REPEATING:
                case WORKBENCH:
                case DARK_OAK_DOOR:
                case ACACIA_DOOR:
                case BIRCH_DOOR:
                case IRON_DOOR:
                case JUNGLE_DOOR:
                case SPRUCE_DOOR:
                case TRAP_DOOR:
                case WOOD_DOOR:
                case WOODEN_DOOR:
                case IRON_TRAPDOOR:
                case ENCHANTMENT_TABLE:
                case ENDER_PORTAL_FRAME:
                case FENCE_GATE:
                case ACACIA_FENCE_GATE:
                case BIRCH_FENCE_GATE:
                case DARK_OAK_FENCE_GATE:
                case JUNGLE_FENCE_GATE:
                case SPRUCE_FENCE_GATE:
                case FURNACE:
                case BURNING_FURNACE:
                case JUKEBOX:
                case LEVER:
                case NOTE_BLOCK:
                case REDSTONE_ORE:
                case TRAPPED_CHEST:
                case ENDER_CHEST:
                case REDSTONE_COMPARATOR:
                case REDSTONE_COMPARATOR_OFF:
                case REDSTONE_COMPARATOR_ON:
                case DIODE:
                case DIODE_BLOCK_OFF:
                case DIODE_BLOCK_ON:
                    switch (this.getPlayerInteractStateInChunk(event.getPlayer(), event.getClickedBlock().getChunk())) {
                        case DENIED:
                            event.setCancelled(true);
                            event.getPlayer().sendMessage(Utility.formatResponse("Claims", "You do not have permission to interact with blocks this chunk!", ChatColor.RED));
                            break;
                        case LOADING:
                            event.getPlayer().sendMessage(Utility.formatResponse("Claims", "Please wait a moment...", ChatColor.RED));
                            event.setCancelled(true);
                            break;
                    }

                    break;
            }
        }
    }





    private PermissionState getPlayerInteractStateInChunk(Player player, Chunk chunk) {
        return this.getPlayerStateInChunkUsingHelperPermission(player, chunk, ClaimHelper.Permission.INTERACT);
    }

    private PermissionState getPlayerBuildStateInChunk(Player player, Chunk chunk) {
        return this.getPlayerStateInChunkUsingHelperPermission(player, chunk, ClaimHelper.Permission.BUILD);
    }

    /**
     * Sees if the player can perform an operation on the chunk
     * by checking if the chunk has no owner or if the chunk owner is the player.
     *
     * If this is not the case, it will get the cached claim helpers
     * and check to see if they have the permission provided.
     */
    private PermissionState getPlayerStateInChunkUsingHelperPermission(Player player, Chunk chunk, ClaimHelper.Permission helperPermission) {
        if (player.hasPermission(Permissions.HAS_CLAIM_ADMIN)) {
            return PermissionState.ALLOWED;
        }
        ChunkCoordinates coordinates = new ChunkCoordinates(
                chunk.getWorld().getUID(),
                chunk.getX(),
                chunk.getZ()
        );
        Optional<Claim> currentClaim = this.plugin.getClaimsManager().getClaim(coordinates);

        if (!currentClaim.isPresent()) {
            return PermissionState.LOADING;
        }

        // does this claim have a owner? Is it us? If not, are we a helper with permission?
        if (currentClaim.get().getOwner().isPresent() && !currentClaim.get().getOwner().get().equals(player.getUniqueId())) {
            // We do not own this claim, so are we a claim helper with permission to do this?
            Optional<ClaimHelper> helper = this.plugin.getClaimsManager().getClaimHelper(coordinates, player.getUniqueId());
            if (!helper.isPresent() || !helper.get().hasPermission(helperPermission)) {
                return PermissionState.DENIED;
            }
        }

        return PermissionState.ALLOWED;
    }

    private enum PermissionState {
        ALLOWED,
        DENIED,
        LOADING
    }

}
