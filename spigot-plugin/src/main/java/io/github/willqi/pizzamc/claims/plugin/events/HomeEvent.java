package io.github.willqi.pizzamc.claims.plugin.events;

import io.github.willqi.pizzamc.claims.api.homes.Home;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerEvent;

public abstract class HomeEvent extends PlayerEvent {

    private Home home;

    public HomeEvent(Player player, Home home) {
        super(player);
        this.home = home;
    }

    public Home getHome() {
        return this.home;
    }

}
