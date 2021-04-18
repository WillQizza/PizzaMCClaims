package io.github.willqi.pizzamc.claims.plugin.menus.types;

import io.github.willqi.pizzamc.claims.api.homes.Home;
import io.github.willqi.pizzamc.claims.plugin.ClaimsPlugin;
import io.github.willqi.pizzamc.claims.plugin.Utility;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class HomeInformationType implements MenuType {

    public static final String ID = "home_information";

    private final Map<UUID, Home> openInventories;

    private final ClaimsPlugin plugin;

    public HomeInformationType(ClaimsPlugin plugin) {
        this.openInventories = new HashMap<>();
        this.plugin = plugin;
    }

    @Override
    public void onOpen(Player player, Map<String, Object> params) {
        Home home = (Home)params.get("home");
        Inventory homeInformationInventory = Bukkit.createInventory(player, 9, "Home - " + home.getName());

        for (int i = 0; i < 9; i++) {
            switch (i) {
                case 0:
                    homeInformationInventory.setItem(i, getUpdateHomeItem());
                    break;
                case 4:
                    homeInformationInventory.setItem(i, getTeleportItem(home));
                    break;
                case 8:
                    homeInformationInventory.setItem(i, getDeleteItem());
                    break;
                default:
                    homeInformationInventory.setItem(i, getEmptyGlassPane());
                    break;
            }
        }

        player.openInventory(homeInformationInventory);
        this.openInventories.put(player.getUniqueId(), home);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof Player) {
            Player player = (Player) event.getInventory().getHolder();
            if (this.openInventories.containsKey(player.getUniqueId()) && event.getClickedInventory() != player.getInventory()) {
                event.setCancelled(true);
                Home home = this.openInventories.get(player.getUniqueId());

                switch (event.getSlot()) {
                    case 0:
                        player.closeInventory();
                        this.plugin.getMenuManager().closeMenu(player);
                        home.setX(player.getLocation().getX());
                        home.setY(player.getLocation().getY());
                        home.setZ(player.getLocation().getZ());
                        home.setWorldUuid(player.getLocation().getWorld().getUID());
                        this.plugin.getHomesManager().save(home).whenComplete((v, exception) -> {
                            if (exception != null) {
                                exception.printStackTrace();
                                player.sendMessage(Utility.formatResponse("Homes", "An exception has occurred.", ChatColor.RED));
                            } else {
                                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
                                player.sendMessage(Utility.formatResponse("Homes", "Updated home!", ChatColor.GREEN));
                            }
                        });
                        break;
                    case 4:
                        player.closeInventory();
                        this.plugin.getMenuManager().closeMenu(player);
                        player.teleport(
                                new Location(
                                        player.getServer().getWorld(home.getWorldUuid()),
                                        home.getX(),
                                        home.getY(),
                                        home.getZ()
                                ),
                                PlayerTeleportEvent.TeleportCause.PLUGIN
                        );
                        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMEN_TELEPORT, 1f, 1f);
                        player.sendMessage(Utility.formatResponse("Homes", "Teleported!"));
                        break;
                    case 8:
                        Map<String, Object> params = new HashMap<>();
                        params.put("home", home);
                        this.plugin.getMenuManager().showMenu(player, DeleteHomeConfirmationType.ID, params);
                        break;
                }
            }
        }
    }

    @EventHandler
    public void onAnotherInventoryOpen(InventoryOpenEvent event) {
        // In case another plugin opens the inventory when we already have our inventory open
        this.openInventories.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        this.openInventories.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        this.openInventories.remove(event.getPlayer().getUniqueId());
    }




    private static ItemStack getUpdateHomeItem() {
        ItemStack item = new ItemStack(Material.COMPASS);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("" + ChatColor.RESET + ChatColor.AQUA + "Update Position");
        meta.setLore(Collections.singletonList("Click to change your home to here!"));
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack getDeleteItem() {
        ItemStack item = new ItemStack(Material.TNT, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("" + ChatColor.RESET + ChatColor.RED + "Delete Home");
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack getTeleportItem(Home home) {
        ItemStack item = new ItemStack(Material.BED, 1, (short)14);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("" + ChatColor.RESET + ChatColor.LIGHT_PURPLE + "Teleport to " + home.getName());
        meta.setLore(
                Collections.singletonList(
                    "" + ChatColor.RESET + ChatColor.YELLOW + Bukkit.getWorld(home.getWorldUuid()).getName() +
                    ChatColor.WHITE + " (" + ChatColor.YELLOW +
                    roundCoordinate(home.getX()) + ", " +
                    roundCoordinate(home.getY()) + ", " +
                    roundCoordinate(home.getZ()) +
                    ChatColor.WHITE + ")"
                )
        );
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack getEmptyGlassPane() {
        ItemStack emptyGlassPane = new ItemStack(Material.STAINED_GLASS_PANE, 1,  (short)7);
        ItemMeta meta = emptyGlassPane.getItemMeta();
        meta.setDisplayName(" ");
        emptyGlassPane.setItemMeta(meta);
        return emptyGlassPane;
    }

    private static double roundCoordinate(double coordinate) {
        return Double.parseDouble(BigDecimal.valueOf(coordinate).setScale(2, RoundingMode.FLOOR).toString());
    }

}
