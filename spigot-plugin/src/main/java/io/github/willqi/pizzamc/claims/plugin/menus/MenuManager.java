package io.github.willqi.pizzamc.claims.plugin.menus;

import io.github.willqi.pizzamc.claims.plugin.ClaimsPlugin;
import io.github.willqi.pizzamc.claims.plugin.menus.types.MenuType;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.*;

public class MenuManager implements Listener {

    private final ClaimsPlugin plugin;

    private final Map<String, MenuType> menuTypes;
    private final Map<UUID, MenuType> activePlayerMenus;

    public MenuManager (ClaimsPlugin plugin) {
        this.plugin = plugin;
        this.menuTypes = new HashMap<>();
        this.activePlayerMenus = new HashMap<>();

        this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
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
