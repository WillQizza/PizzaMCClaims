package io.github.willqi.pizzamc.claims.plugin.events;

import io.github.willqi.pizzamc.claims.api.claims.Claim;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerEvent;

public abstract class ClaimEvent extends PlayerEvent {

    private final Claim claim;

    public ClaimEvent(Player player, Claim claim) {
        super(player);
        this.claim = claim;
    }

    public Claim getClaim() {
        return this.claim;
    }

}
