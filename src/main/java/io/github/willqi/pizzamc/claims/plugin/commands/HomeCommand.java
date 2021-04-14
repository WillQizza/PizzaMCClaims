package io.github.willqi.pizzamc.claims.plugin.commands;

import io.github.willqi.pizzamc.claims.plugin.ClaimsPlugin;
import io.github.willqi.pizzamc.claims.plugin.Utility;
import io.github.willqi.pizzamc.claims.api.homes.Home;
import io.github.willqi.pizzamc.claims.api.homes.HomesManager;
import io.github.willqi.pizzamc.claims.api.homes.exceptions.InvalidHomeNameException;
import org.bukkit.ChatColor;
import org.bukkit.Location;
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

    public static final String PERMISSION = "pizzamcclaims.commands.home";

    private final ClaimsPlugin plugin;

    public HomeCommand (final ClaimsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(final CommandSender commandSender, final Command command, final String label, final String[] args) {

        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(Utility.formatResponse("Homes", "You can only run this command as a player.", ChatColor.RED));
            return true;
        }
        final Player player = (Player)commandSender;

        if (!player.hasPermission(PERMISSION)) {
            commandSender.sendMessage(Utility.formatResponse("Permissions", "You do not have permission to run this command.", ChatColor.RED));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(Utility.formatResponse("Homes", USAGE_MESSAGE));
            return true;
        }

        final HomesManager homesManager = plugin.getHomesManager();

        if (!homesManager.hasHomesLoaded(player)) {
            player.sendMessage(Utility.formatResponse("Homes", "Please wait a moment...", ChatColor.RED));
            return true;
        }

        final Map<String, Home> playerHomes = homesManager.getHomes(player);

        Optional<Home> targetHome;
        switch (args[0].toLowerCase()) {
            case "list":
                plugin.getMenuManager().sendHomeMenu(player);
                break;
            case "teleport":
            case "tp":

                if (args.length < 2) {
                    player.sendMessage(Utility.formatResponse("Homes", String.format("Incorrect usage: /%s %s <name>", label, args[0].toLowerCase()), ChatColor.RED));
                    return true;
                }
                targetHome = homesManager.getHome(player, String.join(" ", Arrays.copyOfRange(args, 1, args.length)));
                if (!targetHome.isPresent()) {
                    player.sendMessage(Utility.formatResponse("Homes", "No home could be found by that name!", ChatColor.RED));
                    return true;
                }
                player.teleport(
                        new Location(
                                player.getServer().getWorld(targetHome.get().getLevelUUID()),
                                targetHome.get().getX(),
                                targetHome.get().getY(),
                                targetHome.get().getZ()
                        ),
                        TeleportCause.PLUGIN
                );
                player.sendMessage(Utility.formatResponse("Homes", "Teleported!"));

                break;
            case "create":
            case "add":
                if (args.length < 2) {
                    player.sendMessage(Utility.formatResponse("Homes", String.format("Incorrect usage: /%s %s <name>", label, args[0].toLowerCase()), ChatColor.RED));
                    return true;
                }
                if (!homesManager.canCreateHome(player)) {
                    player.sendMessage(Utility.formatResponse("Homes", "You have reached the max homes limit!", ChatColor.RED));
                    return true;
                }
                try {
                    homesManager.createHome(player, String.join(" ", Arrays.copyOfRange(args, 1, args.length)));
                } catch (InvalidHomeNameException exception) {
                    player.sendMessage(Utility.formatResponse("Homes", exception.getMessage(), ChatColor.RED));
                    return true;
                }
                player.sendMessage(Utility.formatResponse("Home", "Home created!", ChatColor.GREEN));
                break;
            case "remove":
            case "destroy":
            case "delete":
                if (args.length < 2) {
                    player.sendMessage(Utility.formatResponse("Homes", String.format("Incorrect usage: /%s %s <name>", label, args[0].toLowerCase()), ChatColor.RED));
                    return true;
                }
                targetHome = homesManager.getHome(player, String.join(" ", Arrays.copyOfRange(args, 1, args.length)));
                if (!targetHome.isPresent()) {
                    player.sendMessage(Utility.formatResponse("Homes", "No home could be found by that name!", ChatColor.RED));
                    return true;
                }
                targetHome.get().destroy();
                player.sendMessage(Utility.formatResponse("Homes", "Deleted home!", ChatColor.GREEN));
                break;
            case "details":
            case "detail":
                if (args.length < 2) {
                    player.sendMessage(Utility.formatResponse("Homes", String.format("Incorrect usage: /%s %s <name>", label, args[0].toLowerCase()), ChatColor.RED));
                    return true;
                }
                targetHome = homesManager.getHome(player, String.join(" ", Arrays.copyOfRange(args, 1, args.length)));
                if (!targetHome.isPresent()) {
                    player.sendMessage(Utility.formatResponse("Homes", "No home could be found by that name!", ChatColor.RED));
                    return true;
                }
                plugin.getMenuManager().sendHomeInformationMenu(player, targetHome.get());
                break;
            default:
                player.sendMessage(Utility.formatResponse("Homes", USAGE_MESSAGE));
                break;
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(final CommandSender commandSender, final Command command, final String s, final String[] args) {

        if (!(commandSender instanceof Player)) {
            return new ArrayList<>();
        }

        List<String> options = new ArrayList<>();
        switch (args.length) {

            case 1:
                Collections.sort(
                    StringUtil.copyPartialMatches(args[0], Arrays.asList("list", "teleport", "create", "destroy", "details"), options)
                );

            case 2:
                switch (args[0].toLowerCase()) {
                    case "teleport":
                    case "destroy":
                    case "details":
                        Collections.sort(
                                StringUtil.copyPartialMatches(args[1], plugin.getHomesManager().getHomes((Player)commandSender).values().stream().map(home -> home.getName()).collect(Collectors.toList()), options)
                        );
                        break;
                }

        }

        return options;
    }

}
