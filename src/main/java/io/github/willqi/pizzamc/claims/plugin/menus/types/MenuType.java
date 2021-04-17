package io.github.willqi.pizzamc.claims.plugin.menus.types;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.Map;

public interface MenuType extends Listener {

    void onOpen(Player player, Map<String, Object> params);
    void onClose(Player player);

}
