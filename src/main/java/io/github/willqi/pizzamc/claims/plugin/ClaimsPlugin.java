package io.github.willqi.pizzamc.claims.plugin;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.pool.HikariPool;
import io.github.willqi.pizzamc.claims.api.claims.ClaimsManager;
import io.github.willqi.pizzamc.claims.api.claims.database.impl.HikariClaimsDao;
import io.github.willqi.pizzamc.claims.api.claims.database.impl.HikariClaimsHelperDao;
import io.github.willqi.pizzamc.claims.api.homes.HomesManager;
import io.github.willqi.pizzamc.claims.api.homes.database.impl.HikariHomesDao;
import io.github.willqi.pizzamc.claims.plugin.commands.HomeCommand;
import io.github.willqi.pizzamc.claims.plugin.listeners.HomeListener;
import io.github.willqi.pizzamc.claims.plugin.listeners.PlayerChunkProtectionListener;
import io.github.willqi.pizzamc.claims.plugin.menus.MenuManager;
import org.bukkit.plugin.java.JavaPlugin;

public class ClaimsPlugin extends JavaPlugin {

    private ClaimsManager claimsManager;
    private HomesManager homesManager;
    private MenuManager menuManager;

    private HikariPool pool;

    @Override
    public void onDisable() {
        if (homesManager != null) {
            homesManager.cleanUp();
        }
        if (claimsManager != null) {
            claimsManager.cleanUp();
        }
        if (this.pool != null)  {
            try {
                this.pool.shutdown();
            } catch (InterruptedException exception) {
                this.getLogger().severe("Failed to properly close Hikari pool.");
                exception.printStackTrace();
            }
        }
    }

    @Override
    public void onEnable() {

        this.saveDefaultConfig();
        this.registerEvents();
        this.registerCommands();

        HikariConfig dbConfig = new HikariConfig();
        dbConfig.setJdbcUrl("jdbc:mysql://" + this.getConfig().getString("host") + ":" + this.getConfig().getInt("port") + "/" + this.getConfig().getString("database"));
        dbConfig.setUsername(this.getConfig().getString("username"));
        dbConfig.setPassword(this.getConfig().getString("password"));
        this.pool = new HikariPool(dbConfig);

        this.claimsManager = new ClaimsManager(new HikariClaimsDao(this.pool), new HikariClaimsHelperDao(this.pool));
        this.homesManager = new HomesManager(new HikariHomesDao());
        this.menuManager = new MenuManager(this);


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

    private void registerEvents () {

        this.getServer().getPluginManager().registerEvents(new PlayerChunkProtectionListener(), this);
        this.getServer().getPluginManager().registerEvents(new HomeListener(this.getHomesManager()), this);

    }

    private void registerCommands () {

        HomeCommand homeCommand = new HomeCommand(this);
        this.getCommand("home").setExecutor(homeCommand);
        this.getCommand("home").setTabCompleter(homeCommand);

    }

}
