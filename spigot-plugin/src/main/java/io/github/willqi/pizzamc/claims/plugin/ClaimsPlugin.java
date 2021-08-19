package io.github.willqi.pizzamc.claims.plugin;

import io.github.willqi.pizzamc.claims.api.claims.ClaimsManager;
import io.github.willqi.pizzamc.claims.api.daosources.DaoSource;
import io.github.willqi.pizzamc.claims.api.daosources.SQLDaoSource;
import io.github.willqi.pizzamc.claims.api.exceptions.DaoException;
import io.github.willqi.pizzamc.claims.api.homes.HomesManager;
import io.github.willqi.pizzamc.claims.api.users.UsersManager;
import io.github.willqi.pizzamc.claims.plugin.commands.ClaimCommand;
import io.github.willqi.pizzamc.claims.plugin.commands.HomeCommand;
import io.github.willqi.pizzamc.claims.plugin.listeners.ClaimListener;
import io.github.willqi.pizzamc.claims.plugin.listeners.HomeListener;
import io.github.willqi.pizzamc.claims.plugin.listeners.UsersListener;
import io.github.willqi.pizzamc.claims.plugin.menus.MenuManager;
import io.github.willqi.pizzamc.claims.plugin.menus.types.*;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public class ClaimsPlugin extends JavaPlugin {

    private ClaimsManager claimsManager;
    private HomesManager homesManager;
    private MenuManager menuManager;
    private UsersManager usersManager;

    private DaoSource daoSource;

    @Override
    public void onDisable() {
        if (homesManager != null) {
            homesManager.cleanUp();
        }
        if (claimsManager != null) {
            claimsManager.cleanUp();
        }
        if (this.daoSource != null)  {
            this.daoSource.cleanUp();
        }
    }

    @Override
    public void onEnable() {
        this.saveDefaultConfig();

        try {
            this.daoSource = new SQLDaoSource(
                    this.getConfig().getString("host"),
                    this.getConfig().getInt("port"),
                    this.getConfig().getString("database"),
                    this.getConfig().getString("username"),
                    this.getConfig().getString("password")
            );
        } catch (DaoException exception) {
            this.getLogger().log(Level.SEVERE, "Cannot connect to database.", exception);
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.claimsManager = new ClaimsManager(this.daoSource.getClaimsDao(), this.daoSource.getClaimsHelperDao());
        this.homesManager = new HomesManager(this.daoSource.getHomesDao());
        this.usersManager = new UsersManager(this.daoSource.getUsersDao());
        this.menuManager = new MenuManager(this);

        this.registerEvents();
        this.registerCommands();
        this.registerMenuTypes();
    }

    public ClaimsManager getClaimsManager() {
        return this.claimsManager;
    }

    public HomesManager getHomesManager() {
        return this.homesManager;
    }

    public UsersManager getUsersManager() {
        return this.usersManager;
    }

    public MenuManager getMenuManager() {
        return this.menuManager;
    }

    private void registerEvents() {
        this.getServer().getPluginManager().registerEvents(new HomeListener(this), this);
        this.getServer().getPluginManager().registerEvents(new ClaimListener(this), this);
        this.getServer().getPluginManager().registerEvents(new UsersListener(this), this);
    }

    private void registerCommands() {
        HomeCommand homeCommand = new HomeCommand(this);
        this.getCommand("home").setExecutor(homeCommand);
        this.getCommand("home").setTabCompleter(homeCommand);

        ClaimCommand claimCommand = new ClaimCommand(this);
        this.getCommand("claim").setExecutor(claimCommand);
        this.getCommand("claim").setTabCompleter(claimCommand);
    }

    private void registerMenuTypes() {
        this.menuManager.register(HomeSelectionMenuType.ID, new HomeSelectionMenuType(this));
        this.menuManager.register(HomeInformationType.ID, new HomeInformationType(this));
        this.menuManager.register(DeleteHomeConfirmationType.ID, new DeleteHomeConfirmationType(this));

        this.menuManager.register(ClaimFlagsType.ID, new ClaimFlagsType(this));
        this.menuManager.register(ClaimHelperSelectionMenuType.ID, new ClaimHelperSelectionMenuType(this));
        this.menuManager.register(ClaimHelperFlagsType.ID, new ClaimHelperFlagsType(this));
        this.menuManager.register(ClaimHelperLookUpType.ID, new ClaimHelperLookUpType(this));
    }

}
