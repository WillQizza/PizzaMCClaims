package io.github.willqi.pizzamc.claims;

import io.github.willqi.pizzamc.claims.api.ClaimsManager;
import io.github.willqi.pizzamc.claims.database.PizzaSQLDatabase;
import io.github.willqi.pizzamc.claims.listeners.ChunkClaimStorageListener;
import io.github.willqi.pizzamc.claims.listeners.PlayerChunkProtectionListener;
import org.bukkit.plugin.java.JavaPlugin;

public class ClaimsPlugin extends JavaPlugin {

    private ClaimsManager claimsManager;
    private PizzaSQLDatabase database;

    @Override
    public void onEnable() {

        saveDefaultConfig();
        registerEvents();

        database = new PizzaSQLDatabase(
            getConfig().getString("host"),
            getConfig().getInt("port"),
            getConfig().getString("database"),
            getConfig().getString("username"),
            getConfig().getString("password")
        );
        claimsManager = new ClaimsManager(this);
    }

    public ClaimsManager getClaimsManager () {
        return claimsManager;
    }

    public PizzaSQLDatabase getDatabase () {
        return database;
    }

    private void registerEvents () {

        getServer().getPluginManager().registerEvents(new ChunkClaimStorageListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerChunkProtectionListener(), this);

    }

}
