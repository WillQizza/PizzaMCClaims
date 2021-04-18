package io.github.willqi.pizzamc.claims.plugin.menus;

import io.github.willqi.pizzamc.claims.plugin.ClaimsPlugin;
import io.github.willqi.pizzamc.claims.plugin.Utility;
import io.github.willqi.pizzamc.claims.api.homes.Home;
import io.github.willqi.pizzamc.claims.plugin.menus.types.MenuType;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class MenuManager implements Listener {

    private static final int BACK_ARROW_INDEX = 0;
    private static final int FORWARD_ARROW_INDEX = 8;
    private static final String NBT_HOME_ID = "io.github.willqi.home_name";

    private final ClaimsPlugin plugin;

    private static final int HOME_MENU_SIZE = 54; // Multiple of 9 greater than or equal to 18
    private static final int HOME_MENU_ITEMS_PER_PAGE = HOME_MENU_SIZE - 9;

    private final Map<String, MenuType> menuTypes;
    private final Map<UUID, MenuType> activePlayerMenus;

    public MenuManager (ClaimsPlugin plugin) {
        this.plugin = plugin;
        this.menuTypes = new HashMap<>();
        this.activePlayerMenus = new HashMap<>();
    }

    public void register(String id, MenuType menuType) {
        this.plugin.getServer().getPluginManager().registerEvents(menuType, this.plugin);
        this.menuTypes.put(id, menuType);
    }

    public void showMenu(Player player, String menuType) {
        this.showMenu(player, menuType, new HashMap<>());
    }

    public void showMenu(Player player, String menuTypeId, Map<String, Object> params) {
        this.closeMenu(player);
        // 1 tick delay to ensure that changing menus mid event listener iterating does not cause processing of the same event.
        this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, () -> {
            MenuType menuType = this.menuTypes.get(menuTypeId);
            this.activePlayerMenus.put(player.getUniqueId(), menuType);
            menuType.onOpen(player, params);
        }, 1);
    }

    public void closeMenu(Player player) {
        MenuType menuType = this.activePlayerMenus.remove(player.getUniqueId());
        if (menuType != null) {
            menuType.onClose(player);
        }
    }

}
