package io.github.willqi.pizzamc.claims.plugin.menus.types;


import io.github.willqi.pizzamc.claims.api.homes.Home;
import io.github.willqi.pizzamc.claims.plugin.ClaimsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

public class HomeSelectionMenuType implements MenuType {

    public static final String ID = "home_selection_menu";

    private final ClaimsPlugin plugin;
    private final Map<UUID, Integer> openInventories;

    public HomeSelectionMenuType(ClaimsPlugin plugin) {
        this.plugin = plugin;
        this.openInventories = new HashMap<>();
    }

    @Override
    public void onOpen(Player player, Map<String, Object> params) {
        int currentPage = (int)params.getOrDefault("page", 1);

        List<Home> homes = this.getHomes(player.getUniqueId());

        Inventory homesSelectionInventory = Bukkit.createInventory(player, 54, "My Homes");

        for (int i = 1; i < 8; i++) {
            homesSelectionInventory.setItem(i, getEmptyGlassPane());
        }

        // Do we need the previous page button?
        if (currentPage > 1) {
            homesSelectionInventory.setItem(0, getPreviousPageItemStack(currentPage));
        } else {
            homesSelectionInventory.setItem(0, getNoPageOptionItemStack());
        }

        // Do we need the next page button?
        int skippedHomes = (currentPage - 1) * 45;
        if (homes.size() - skippedHomes > 45) {
            homesSelectionInventory.setItem(8, getNextPageItemStack(currentPage));
        } else {
            homesSelectionInventory.setItem(8, getNoPageOptionItemStack());
        }

        // Place homes on inventory
        List<Home> shownHomes = homes.subList((currentPage - 1) * 45, Math.min(currentPage * 45, homes.size()));
        int inventoryIndex = 9;
        for (Home home : shownHomes) {
            homesSelectionInventory.setItem(inventoryIndex, getHomeItemStack(home));
            inventoryIndex++;
        }

        player.openInventory(homesSelectionInventory);
        this.openInventories.put(player.getUniqueId(), currentPage);
    }

    @Override
    public void onClose(Player player) {
        player.closeInventory();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof Player) {
            Player player = (Player)event.getInventory().getHolder();
            if (this.openInventories.containsKey(player.getUniqueId()) && event.getClickedInventory() != player.getInventory()) {
                event.setCancelled(true);

                int currentPage = this.openInventories.get(player.getUniqueId());
                switch (event.getSlot()) {
                    case 0:
                        if (event.getCurrentItem().getDurability() != (short)7) {
                            // Go back button
                            Map<String, Object> params = new HashMap<>();
                            params.put("page", currentPage - 1);
                            this.plugin.getMenuManager().showMenu(player, ID, params);
                        }
                        break;
                    case 8:
                        if (event.getCurrentItem().getDurability() != (short)7) {
                            // Go forward button
                            Map<String, Object> params = new HashMap<>();
                            params.put("page", currentPage + 1);
                            this.plugin.getMenuManager().showMenu(player, ID, params);
                        }
                        break;
                    default:
                        if (event.getSlot() > 8) {
                            int homeIndex = event.getSlot() - 9 + (currentPage - 1) * 45;
                            List<Home> homes = this.getHomes(player.getUniqueId());
                            if (homes.size() > homeIndex) {
                                Home home = homes.get(homeIndex);
                                Map<String, Object> params = new HashMap<>();
                                params.put("home", home);
                                this.plugin.getMenuManager().showMenu(player, HomeInformationType.ID, params);
                            }
                        }
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


    private List<Home> getHomes(UUID uuid) {
        Optional<Map<String, Home>> cachedHomes = this.plugin.getHomesManager().getHomes(uuid);
        if (cachedHomes.isPresent()) {
            return cachedHomes.get().values()
                    .stream().sorted(Comparator.comparing(Home::getName))
                    .collect(Collectors.toList());
        } else {
            return new ArrayList<>();
        }

    }

    private static ItemStack getPreviousPageItemStack(int currentPage) {
        ItemStack goBackItem = new Wool(DyeColor.RED).toItemStack(1);
        ItemMeta meta = goBackItem.getItemMeta();
        meta.setDisplayName(ChatColor.RESET + "Previous Page (" + (currentPage - 1) + ")");
        goBackItem.setItemMeta(meta);
        return goBackItem;
    }

    private static ItemStack getNextPageItemStack(int currentPage) {
        ItemStack goForwardItem = new Wool(DyeColor.LIME).toItemStack(1);
        ItemMeta meta = goForwardItem.getItemMeta();
        meta.setDisplayName(ChatColor.RESET + "Next Page (" + (currentPage + 1) + ")");
        goForwardItem.setItemMeta(meta);
        return goForwardItem;
    }

    private static ItemStack getNoPageOptionItemStack() {
        ItemStack noOptionItem = new Wool(DyeColor.GRAY).toItemStack(1);
        ItemMeta meta = noOptionItem.getItemMeta();
        meta.setDisplayName(" ");
        noOptionItem.setItemMeta(meta);
        return noOptionItem;
    }

    private static ItemStack getHomeItemStack(Home home) {
        ItemStack homeItem = new ItemStack(Material.BED, 1, (short)14);
        ItemMeta meta = homeItem.getItemMeta();
        meta.setDisplayName("" + ChatColor.RESET + ChatColor.LIGHT_PURPLE + home.getName());

        List<String> lore = new ArrayList<>();
        lore.add("" + ChatColor.RESET + ChatColor.AQUA + "Click to view details");
        lore.add(
                "" + ChatColor.RESET + ChatColor.YELLOW + Bukkit.getWorld(home.getWorldUUID()).getName() +
                ChatColor.WHITE + " (" + ChatColor.YELLOW +
                roundCoordinate(home.getX()) + ", " +
                roundCoordinate(home.getY()) + ", " +
                roundCoordinate(home.getZ()) +
                ChatColor.WHITE + ")"
        );
        meta.setLore(lore);

        homeItem.setItemMeta(meta);
        return homeItem;
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
