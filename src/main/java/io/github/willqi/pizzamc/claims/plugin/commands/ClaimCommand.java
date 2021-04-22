package io.github.willqi.pizzamc.claims.plugin.commands;

import io.github.willqi.pizzamc.claims.api.claims.ChunkCoordinates;
import io.github.willqi.pizzamc.claims.api.claims.Claim;
import io.github.willqi.pizzamc.claims.api.claims.ClaimsManager;
import io.github.willqi.pizzamc.claims.plugin.ClaimsPlugin;
import io.github.willqi.pizzamc.claims.plugin.Permissions;
import io.github.willqi.pizzamc.claims.plugin.Utility;
import io.github.willqi.pizzamc.claims.plugin.events.ChunkClaimEvent;
import io.github.willqi.pizzamc.claims.plugin.events.ChunkUnclaimEvent;
import io.github.willqi.pizzamc.claims.plugin.menus.types.ClaimFlagsType;
import io.github.willqi.pizzamc.claims.plugin.menus.types.ClaimHelperSelectionMenuType;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.*;
import java.util.logging.Level;

public class ClaimCommand implements CommandExecutor, TabCompleter {

    private static final String USAGE_MESSAGE = "Need help using /claim?\n" +
            "/claim add - Claim the chunk you are in\n" +
            "/claim remove - Unclaim the chunk you are in\n" +
            "/claim flags - View/modify the flags of your claim\n" +
            "/claim helpers - View/modify the helpers of your claim";

    private final ClaimsPlugin plugin;

    public ClaimCommand (ClaimsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {

        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(Utility.formatResponse("Claims", "You can only run this command as a player.", ChatColor.RED));
            return true;
        }
        Player player = (Player)commandSender;
        boolean playerIsClaimAdmin = player.hasPermission(Permissions.HAS_CLAIM_ADMIN);

        if (
                !playerIsClaimAdmin &&
                !player.hasPermission(Permissions.CAN_MANAGE_HELPERS) &&
                !player.hasPermission(Permissions.CAN_CLAIM_LAND_AND_USE_COMMAND) &&
                !player.hasPermission(Permissions.CAN_CHANGE_CLAIM_FLAGS)
        ) {
            commandSender.sendMessage(Utility.NO_PERMISSIONS_MESSAGE);
            return true;
        }
        if (args.length == 0) {
            player.sendMessage(Utility.formatResponse("Claims", USAGE_MESSAGE));
            return true;
        }

        ClaimsManager claimsManager = this.plugin.getClaimsManager();
        ChunkCoordinates coordinates = new ChunkCoordinates(player.getLocation().getWorld().getUID(), player.getLocation().getChunk().getX(), player.getLocation().getChunk().getZ());

        Optional<Claim> currentClaim = claimsManager.getClaim(coordinates);
        if (!currentClaim.isPresent()) {
            player.sendMessage(Utility.formatResponse("Claims", "Please wait a moment...", ChatColor.RED));
            return true;
        }

        switch (args[0]) {
            case "add":
                if (!player.hasPermission(Permissions.CAN_CLAIM_LAND_AND_USE_COMMAND) && !playerIsClaimAdmin) {
                    player.sendMessage(Utility.NO_PERMISSIONS_MESSAGE);
                    return true;
                }

                // Is this already owned?
                if (currentClaim.get().getOwner().isPresent() && !playerIsClaimAdmin) {
                    if (!currentClaim.get().getOwner().get().equals(player.getUniqueId())) {
                        player.sendMessage(Utility.formatResponse("Claims", "You already claimed this chunk!", ChatColor.RED));
                    } else {
                        player.sendMessage(Utility.formatResponse("Claims", "Sorry, this chunk was already claimed.", ChatColor.RED));
                    }
                    return true;
                }

                // Check if the player is allowed to claim anymore land
                int claimLimit = this.plugin.getConfig().getInt("max_claims_per_player");
                if (!playerIsClaimAdmin && claimLimit >= 0) {
                    Optional<Integer> totalClaims = this.plugin.getClaimsManager().getClaimCount(player.getUniqueId());
                    if (!totalClaims.isPresent()) {
                        player.sendMessage(Utility.formatResponse("Claims", "Please wait a moment..."));
                        return true;
                    }
                    if (totalClaims.get() >= claimLimit) {
                        player.sendMessage(Utility.formatResponse("Claims", "Sorry, you can not claim any more land!", ChatColor.RED));
                        return true;
                    }
                }

                ChunkClaimEvent chunkClaimEvent = new ChunkClaimEvent(player, currentClaim.get());
                this.plugin.getServer().getPluginManager().callEvent(chunkClaimEvent);
                if (chunkClaimEvent.isCancelled()) {
                    return true;
                }

                currentClaim.get().setOwner(player.getUniqueId());
                claimsManager.saveClaim(currentClaim.get()).whenComplete((v, exception) -> {
                    if (exception != null) {
                        this.plugin.getLogger().log(Level.SEVERE, "Failed to claim chunk", exception);
                        player.sendMessage(Utility.formatResponse("Claims", "An exception occurred while trying to claim this chunk.", ChatColor.RED));
                    } else {
                        player.sendMessage(Utility.formatResponse("Claims", "Claimed!", ChatColor.GREEN));
                    }
                });
                break;


            case "remove":
                if (!player.hasPermission(Permissions.CAN_CLAIM_LAND_AND_USE_COMMAND) && !playerIsClaimAdmin) {
                    player.sendMessage(Utility.NO_PERMISSIONS_MESSAGE);
                    return true;
                }

                if (!currentClaim.get().getOwner().isPresent()) {
                    player.sendMessage(Utility.formatResponse("Claims", "Nobody has claimed this chunk!", ChatColor.RED));
                    return true;
                } else if (!currentClaim.get().getOwner().get().equals(player.getUniqueId()) && !playerIsClaimAdmin) {
                    player.sendMessage(Utility.formatResponse("Claims", "Sorry, you do not own this claim.", ChatColor.RED));
                    return true;
                }

                ChunkUnclaimEvent chunkUnclaimEvent = new ChunkUnclaimEvent(player, currentClaim.get());
                this.plugin.getServer().getPluginManager().callEvent(chunkUnclaimEvent);
                if (chunkUnclaimEvent.isCancelled()) {
                    return true;
                }

                claimsManager.deleteClaim(currentClaim.get()).whenComplete((v, exception) -> {
                    if (exception != null) {
                        this.plugin.getLogger().log(Level.SEVERE, "Failed to delete chunk", exception);
                        player.sendMessage(Utility.formatResponse("Claims", "An exception occurred while trying to delete this claim.", ChatColor.RED));
                    } else {
                        player.sendMessage(Utility.formatResponse("Claims", "Unclaimed!", ChatColor.GREEN));
                    }
                });
                break;


            case "flags":
                if (!player.hasPermission(Permissions.CAN_CHANGE_CLAIM_FLAGS) && !playerIsClaimAdmin) {
                    player.sendMessage(Utility.NO_PERMISSIONS_MESSAGE);
                    return true;
                }

                if ((!currentClaim.get().getOwner().isPresent()) || (currentClaim.get().getOwner().get().equals(player.getUniqueId()) && !playerIsClaimAdmin)) {
                    player.sendMessage(Utility.formatResponse("Claims", "Sorry, you do not own this claim.", ChatColor.RED));
                    return true;
                }

                Map<String, Object> flagsParams = new HashMap<>();
                flagsParams.put("claim", currentClaim.get());
                this.plugin.getMenuManager().showMenu(player, ClaimFlagsType.ID, flagsParams);
                break;


            case "helpers":
                if (!player.hasPermission(Permissions.CAN_MANAGE_HELPERS) && !playerIsClaimAdmin) {
                    player.sendMessage(Utility.NO_PERMISSIONS_MESSAGE);
                    return true;
                }

                if ((!currentClaim.get().getOwner().isPresent()) || (currentClaim.get().getOwner().get().equals(player.getUniqueId()) && !playerIsClaimAdmin)) {
                    player.sendMessage(Utility.formatResponse("Claims", "Sorry, you do not own this claim.", ChatColor.RED));
                    return true;
                }

                Map<String, Object> helpersParams = new HashMap<>();
                helpersParams.put("claim", currentClaim.get());
                helpersParams.put("page", 1);
                this.plugin.getMenuManager().showMenu(player, ClaimHelperSelectionMenuType.ID, helpersParams);
                break;
        }

        return true;

    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String label, String[] args) {
        if (!(commandSender instanceof Player)) {
            return null;
        } else {
            List<String> options = new ArrayList<>();
            if (args.length == 1) {
                Collections.sort(
                        StringUtil.copyPartialMatches(args[0], Arrays.asList("add", "remove", "flags", "helpers"), options)
                );
            }
            return options;
        }
    }
}
