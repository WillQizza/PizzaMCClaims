package io.github.willqi.pizzamc.claims.plugin.events;

import io.github.willqi.pizzamc.claims.api.claims.Claim;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class ChunkClaimEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private final Claim claim;
    private boolean cancelled;

    public ChunkClaimEvent(Player player, Claim claim) {
        super(player);
        this.claim = claim;
    }

    public Claim getClaim() {
        return this.claim;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
