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
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Wool;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class DeleteHomeConfirmationType implements MenuType {

    public static final String ID = "delete_home_confirmation";

    private final Map<UUID, Home> openInventories;

    private final ClaimsPlugin plugin;

    public DeleteHomeConfirmationType(ClaimsPlugin plugin) {
        this.plugin = plugin;
        this.openInventories = new HashMap<>();
    }

    @Override
    public void onOpen(Player player, Map<String, Object> params) {
        Home home = (Home)params.get("home");
        Inventory inventory = Bukkit.createInventory(player, 9, "Delete " + home.getName() + "?");
        inventory.setItem(0, getNoItemStack());
        inventory.setItem(4, getHomeItemStack(home));
        inventory.setItem(8, getYesItemStack(home));
        player.openInventory(inventory);
        this.openInventories.put(player.getUniqueId(), home);
    }

    @Override
    public void onClose(Player player) {
        player.closeInventory();
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
                        player.sendMessage(Utility.formatResponse("Homes", "Cancelled."));
                        break;
                    case 8:
                        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_PLACE, 0.5f, 1f);
                        player.closeInventory();
                        this.plugin.getMenuManager().closeMenu(player);
                        this.plugin.getHomesManager().delete(home).whenComplete((v, exception) -> {
                            if (exception != null) {
                                this.plugin.getLogger().log(Level.SEVERE, "An exception occurred while trying to delete a home.", exception);
                                player.sendMessage(Utility.formatResponse("Homes", "An exception has occurred.", ChatColor.RED));
                            } else {
                                player.sendMessage(Utility.formatResponse("Homes", "Deleted home!", ChatColor.GREEN));
                            }
                        });
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


    private static ItemStack getHomeItemStack(Home home) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("" + ChatColor.RESET + ChatColor.RED + "Are you sure you want to delete " + ChatColor.BOLD + home.getName() + ChatColor.RESET + ChatColor.RED + "?");
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack getYesItemStack(Home home) {
        ItemStack item = new Wool(DyeColor.LIME).toItemStack(1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("" + ChatColor.RESET + ChatColor.RED + "Are you sure you want to delete " + ChatColor.BOLD + home.getName() + ChatColor.RESET + ChatColor.RED + "?");
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack getNoItemStack() {
        ItemStack item = new Wool(DyeColor.RED).toItemStack(1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.RESET + "Cancel");
        item.setItemMeta(meta);
        return item;
    }

}
