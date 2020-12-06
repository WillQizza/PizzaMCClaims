package io.github.willqi.pizzamc.claims;

import io.github.willqi.pizzamc.claims.api.claims.ClaimsManager;
import io.github.willqi.pizzamc.claims.api.homes.HomesManager;
import io.github.willqi.pizzamc.claims.commands.HomeCommand;
import io.github.willqi.pizzamc.claims.database.PizzaSQLDatabase;
import io.github.willqi.pizzamc.claims.listeners.HomeListener;
import io.github.willqi.pizzamc.claims.listeners.PlayerChunkProtectionListener;
import io.github.willqi.pizzamc.claims.menus.MenuManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class ClaimsPlugin extends JavaPlugin {

    private ClaimsManager claimsManager;
    private HomesManager homesManager;
    private MenuManager menuManager;
    private PizzaSQLDatabase database;

    @Override
    public void onDisable() {
        if (homesManager != null) {
            homesManager.cleanUp();
        }
        if (claimsManager != null) {
            claimsManager.cleanUp();
        }
    }

    @Override
    public void onEnable() {

        saveDefaultConfig();
        registerEvents();
        registerCommands();

        database = new PizzaSQLDatabase(
            getConfig().getString("host"),
            getConfig().getInt("port"),
            getConfig().getString("database"),
            getConfig().getString("username"),
            getConfig().getString("password")
        );
        claimsManager = new ClaimsManager(this);
        homesManager = new HomesManager(this);
        menuManager = new MenuManager(this);
    }

    public ClaimsManager getClaimsManager () {
        return claimsManager;
    }

    public HomesManager getHomesManager () {
        return homesManager;
    }

    public MenuManager getMenuManager () {
        return menuManager;
    }

    public PizzaSQLDatabase getDatabase () {
        return database;
    }

    private void registerEvents () {

        getServer().getPluginManager().registerEvents(new PlayerChunkProtectionListener(), this);
        getServer().getPluginManager().registerEvents(new HomeListener(this), this);

    }

    private void registerCommands () {
        final PluginCommand homePluginCommand = getCommand("home");
        final HomeCommand homeCommand = new HomeCommand(this);
        homePluginCommand.setExecutor(homeCommand);
        homePluginCommand.setTabCompleter(homeCommand);
    }

}
