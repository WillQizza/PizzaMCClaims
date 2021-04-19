package io.github.willqi.pizzamc.claims.plugin.commands;

import io.github.willqi.pizzamc.claims.plugin.ClaimsPlugin;
import io.github.willqi.pizzamc.claims.plugin.Permissions;
import io.github.willqi.pizzamc.claims.plugin.Utility;
import io.github.willqi.pizzamc.claims.api.homes.Home;
import io.github.willqi.pizzamc.claims.api.homes.HomesManager;
import io.github.willqi.pizzamc.claims.api.exceptions.InvalidHomeNameException;
import io.github.willqi.pizzamc.claims.plugin.menus.types.HomeInformationType;
import io.github.willqi.pizzamc.claims.plugin.menus.types.HomeSelectionMenuType;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.util.StringUtil;

import java.util.*;
import java.util.stream.Collectors;

public class HomeCommand implements CommandExecutor, TabCompleter {

    private static final String USAGE_MESSAGE = "Need help using /home?\n" +
                                                "/home list - Lists all homes you have\n" +
                                                "/home teleport <name> - Teleport to a home\n" +
                                                "/home create <name> - Create a home\n" +
                                                "/home destroy <name> - Destroy a home\n" +
                                                "/home details <name> - Modify/view your home";

    private final ClaimsPlugin plugin;

    public HomeCommand (ClaimsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {

        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(Utility.formatResponse("Homes", "You can only run this command as a player.", ChatColor.RED));
            return true;
        }
        Player player = (Player)commandSender;

        if (!player.hasPermission(Permissions.CAN_USE_HOME_COMMAND)) {
            commandSender.sendMessage(Utility.NO_PERMISSIONS_MESSAGE);
            return true;
        }
        if (args.length == 0) {
            player.sendMessage(Utility.formatResponse("Homes", USAGE_MESSAGE));
            return true;
        }

        HomesManager homesManager = this.plugin.getHomesManager();
        Optional<Map<String, Home>> homes = homesManager.getHomes(player.getUniqueId());
        if (!homes.isPresent()) {
            player.sendMessage(Utility.formatResponse("Homes", "Please wait a moment...", ChatColor.RED));
            return true;
        }

        Optional<Home> targetHome;
        switch (args[0].toLowerCase()) {
            case "list":
                this.plugin.getMenuManager().showMenu(player, HomeSelectionMenuType.ID);
                break;
            case "teleport":
            case "tp":
                if (args.length < 2) {
                    player.sendMessage(Utility.formatResponse("Homes", "Incorrect usage: /" + label + " " + args[0].toLowerCase() + " <name>", ChatColor.RED));
                    return true;
                }

                targetHome = homesManager.getHome(player.getUniqueId(), String.join(" ", Arrays.copyOfRange(args, 1, args.length)));
                if (!targetHome.isPresent()) {
                    player.sendMessage(Utility.formatResponse("Homes", "No home could be found by that name!", ChatColor.RED));
                    return true;
                }
                player.teleport(
                        new Location(
                                player.getServer().getWorld(targetHome.get().getWorldUuid()),
                                targetHome.get().getX(),
                                targetHome.get().getY(),
                                targetHome.get().getZ()
                        ),
                        TeleportCause.PLUGIN
                );
                player.playSound(player.getLocation(), Sound.ENTITY_ENDERMEN_TELEPORT, 1f, 1f);
                player.sendMessage(Utility.formatResponse("Homes", "Teleported!"));

                break;
            case "create":
            case "add":
                if (args.length < 2) {
                    player.sendMessage(Utility.formatResponse("Homes", String.format("Incorrect usage: /%s %s <name>", label, args[0].toLowerCase()), ChatColor.RED));
                    return true;
                }
                String homeName = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                // TODO: implement userManager to handle home/claim limits
//                if (!homesManager.canCreateHome(player)) {
//                    player.sendMessage(Utility.formatResponse("Homes", "You have reached the max homes limit!", ChatColor.RED));
//                    return true;
//                }

                // Does the home already exist?
                if (homesManager.getHome(player.getUniqueId(), homeName).isPresent()) {
                    player.sendMessage(Utility.formatResponse("Homes", "You already have a home named this!", ChatColor.RED));
                    return true;
                }

                Home createdHome;
                try {
                    createdHome = new Home(
                            player.getUniqueId(),
                            homeName,
                            player.getLocation().getWorld().getUID(),
                            player.getLocation().getX(),
                            player.getLocation().getY(),
                            player.getLocation().getZ()
                    );
                } catch (InvalidHomeNameException exception) {
                    player.sendMessage(Utility.formatResponse("Homes", exception.getMessage(), ChatColor.RED));
                    return true;
                }

                homesManager.save(createdHome).whenCompleteAsync((v, exception) -> {
                    if (exception != null) {
                        exception.printStackTrace();
                        player.sendMessage(Utility.formatResponse("Home", "An exception has occurred.", ChatColor.RED));
                    } else {
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
                        player.sendMessage(Utility.formatResponse("Home", "Home created!", ChatColor.GREEN));
                    }
                });
                break;
            case "remove":
            case "destroy":
            case "delete":
                if (args.length < 2) {
                    player.sendMessage(Utility.formatResponse("Homes", String.format("Incorrect usage: /%s %s <name>", label, args[0].toLowerCase()), ChatColor.RED));
                    return true;
                }

                targetHome = homesManager.getHome(player.getUniqueId(), String.join(" ", Arrays.copyOfRange(args, 1, args.length)));
                if (!targetHome.isPresent()) {
                    player.sendMessage(Utility.formatResponse("Homes", "No home could be found by that name!", ChatColor.RED));
                    return true;
                }

                homesManager.delete(targetHome.get()).whenComplete((v, exception) -> {
                    if (exception != null) {
                        exception.printStackTrace();
                        player.sendMessage(Utility.formatResponse("Homes", "An exception has occurred.", ChatColor.RED));
                    } else {
                        player.sendMessage(Utility.formatResponse("Homes", "Deleted home!", ChatColor.GREEN));
                    }
                });
                break;
            case "details":
            case "detail":
                if (args.length < 2) {
                    player.sendMessage(Utility.formatResponse("Homes", String.format("Incorrect usage: /%s %s <name>", label, args[0].toLowerCase()), ChatColor.RED));
                    return true;
                }

                targetHome = homesManager.getHome(player.getUniqueId(), String.join(" ", Arrays.copyOfRange(args, 1, args.length)));
                if (!targetHome.isPresent()) {
                    player.sendMessage(Utility.formatResponse("Homes", "No home could be found by that name!", ChatColor.RED));
                    return true;
                }
                Map<String, Object> menuParams = new HashMap<>();
                menuParams.put("home", targetHome.get());
                this.plugin.getMenuManager().showMenu(player, HomeInformationType.ID, menuParams);
                break;
            default:
                player.sendMessage(Utility.formatResponse("Homes", USAGE_MESSAGE));
                break;
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String label, String[] args) {

        if (!(commandSender instanceof Player)) {
            return null;
        }
        Player player = (Player)commandSender;

        List<String> options = new ArrayList<>();
        switch (args.length) {

            case 1:
                Collections.sort(
                    StringUtil.copyPartialMatches(args[0], Arrays.asList("list", "teleport", "create", "destroy", "details"), options)
                );
                break;
            case 2:
                switch (args[0].toLowerCase()) {
                    case "teleport":
                    case "destroy":
                    case "details":
                        Optional<Map<String, Home>> homes = this.plugin.getHomesManager().getHomes(player.getUniqueId());
                        homes.ifPresent(homesMap -> Collections.sort(
                                StringUtil.copyPartialMatches(
                                        args[1],
                                        homesMap
                                                .values()
                                                .stream()
                                                .map(Home::getName)
                                                .collect(Collectors.toList()),
                                        options
                                )
                        ));
                        break;
                }
                break;
        }

        return options;
    }

}
