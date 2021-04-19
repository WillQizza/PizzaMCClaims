package io.github.willqi.pizzamc.claims.plugin.menus.types;

import io.github.willqi.pizzamc.claims.plugin.ClaimsPlugin;
import org.bukkit.entity.Player;

import java.util.Map;

public class ClaimHelperSelectionMenuType implements MenuType {

    public static final String ID = "claim_helper_selection_menu";

    private ClaimsPlugin plugin;

    public ClaimHelperSelectionMenuType(ClaimsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onOpen(Player player, Map<String, Object> params) {

    }

}
