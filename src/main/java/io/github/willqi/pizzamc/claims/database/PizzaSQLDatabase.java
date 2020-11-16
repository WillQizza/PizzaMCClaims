package io.github.willqi.pizzamc.claims.database;

import io.github.willqi.pizzamc.claims.ClaimsPlugin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;

/**
 * Responsible for maintaining and handling SQL connection related requests.
 */
public class PizzaSQLDatabase {

    private Connection connection;

    public PizzaSQLDatabase(final String host, final int port, final String database, final String username, final String password) {
        createConnection(host, port, database, username, password);
    }

    public Connection getConnection () {
        return connection;
    }

    private void createConnection (final String host, final int port, final String database, final String username, final String password) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(
                    String.format(
                            "jdbc:mysql://%s:%s/%s",
                            host,
                            port,
                            database
                    ),
                    username,
                    password);
        } catch (ClassNotFoundException | SQLException exception) {
            final ClaimsPlugin plugin = ClaimsPlugin.getPlugin(ClaimsPlugin.class);
            plugin.getLogger().log(Level.SEVERE, "Failed to connect to database. Disabling plugin.");
            plugin.getPluginLoader().disablePlugin(plugin);
        }
    }

}
