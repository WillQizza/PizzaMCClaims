package io.github.willqi.pizzamc.claims.plugin.listeners;

import io.github.willqi.pizzamc.claims.api.users.User;
import io.github.willqi.pizzamc.claims.plugin.ClaimsPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.logging.Level;

public class UsersListener implements Listener {

    private final ClaimsPlugin plugin;

    public UsersListener(ClaimsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        this.plugin.getUsersManager().fetchUser(event.getPlayer().getUniqueId()).whenComplete((user, exception) -> {
            if (exception != null) {
                this.plugin.getLogger().log(Level.SEVERE, "Failed to fetch user record " + event.getPlayer().getUniqueId(), exception);
            } else {
                if (user.isPresent()) {
                    user.get().setName(event.getPlayer().getName());
                    this.plugin.getUsersManager().save(user.get()).whenComplete((v, saveException) -> {
                        if (saveException != null) {
                            this.plugin.getLogger().log(Level.SEVERE, "Failed to update player record", saveException);
                        }
                    });
                } else {
                    // Add new record
                    User newUser = new User(event.getPlayer().getUniqueId(), event.getPlayer().getName());
                    this.plugin.getUsersManager().save(newUser).whenComplete((v, saveException) -> {
                        if (saveException != null) {
                            this.plugin.getLogger().log(Level.SEVERE, "Failed to save player record", saveException);
                        }
                    });
                }
            }
        });
    }

}
