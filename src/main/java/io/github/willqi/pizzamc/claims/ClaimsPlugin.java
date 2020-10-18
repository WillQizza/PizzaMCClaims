package io.github.willqi.pizzamc.claims;

import org.bukkit.plugin.java.JavaPlugin;

public class ClaimsPlugin extends JavaPlugin {

    @Override
    public void onEnable() {

        getServer().getPluginManager().registerEvents(new ChunkInteractionListener(), this);

    }
}
