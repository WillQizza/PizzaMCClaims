package io.github.willqi.pizzamc.claims.api.homes.dao.impl;

import com.zaxxer.hikari.HikariDataSource;
import io.github.willqi.pizzamc.claims.api.exceptions.DaoException;
import io.github.willqi.pizzamc.claims.api.exceptions.InvalidHomeNameException;
import io.github.willqi.pizzamc.claims.api.homes.Home;
import io.github.willqi.pizzamc.claims.api.homes.dao.HomesDao;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class SQLHomesDao implements HomesDao {

    private static final String STMT_CREATE_HOMES_TABLE = "CREATE TABLE IF NOT EXISTS homes (" +
            "ownerUuid VARCHAR(36) NOT NULL," +
            "name VARCHAR(" + Home.MAX_NAME_LENGTH + ") NOT NULL," +
            "worldUuid VARCHAR(36) NOT NULL," +
            "x DOUBLE NOT NULL," +
            "y DOUBLE NOT NULL," +
            "z DOUBLE NOT NULL," +
            "PRIMARY KEY(ownerUuid, name)" +
            ")";
    private static final String STMT_GET_HOMES = "SELECT ownerUuid, name, worldUuid, x, y, z FROM homes WHERE ownerUuid=?";
    private static final String STMT_INSERT_HOME = "INSERT INTO homes (ownerUuid, name, worldUuid, x, y, z) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String STMT_UPDATE_HOME = "UPDATE homes SET worldUuid=?, x=?, y=?, z=? WHERE ownerUuid=? AND name=?";
    private static final String STMT_DELETE_HOME = "DELETE FROM homes WHERE ownerUuid=? AND name=?";

    private HikariDataSource source;

    public SQLHomesDao(HikariDataSource source) throws SQLException {
        this.source = source;
        Connection connection = null;
        Statement stmt = null;
        try {
            connection = this.source.getConnection();
            stmt = connection.createStatement();
            stmt.execute(STMT_CREATE_HOMES_TABLE);
        } finally {
            if (stmt != null) {
                try { stmt.close(); } catch (SQLException ignored) {}
            }
            if (connection != null) {
                try { connection.close(); } catch (SQLException ignored) {}
            }
        }
    }

    @Override
    public Set<Home> getHomesByOwner(UUID uuid) throws DaoException {
        Set<Home> homes = new HashSet<>();
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet results = null;

        try {
            connection = this.source.getConnection();
            stmt = connection.prepareStatement(STMT_GET_HOMES);
            stmt.setString(1, uuid.toString());
            results = stmt.executeQuery();

            while (results.next()) {
                UUID owner = UUID.fromString(results.getString("ownerUuid"));
                UUID worldUuid = UUID.fromString(results.getString("worldUuid"));
                String name = results.getString("name");
                double x = results.getDouble("x");
                double y = results.getDouble("y");
                double z = results.getDouble("z");

                Home home;
                try {
                    home = new Home(owner, name, worldUuid, x, y, z);
                } catch (InvalidHomeNameException exception) {
                    throw new AssertionError("Database contained invalid home name while fetching the homes of " + uuid, exception);
                }
                homes.add(home);
            }

        } catch (SQLException exception) {
            throw new DaoException(exception);
        } finally {
            if (results != null) {
                try { results.close(); } catch (SQLException ignored) {}
            }
            if (stmt != null) {
                try { stmt.close(); } catch (SQLException ignored) {}
            }
            if (connection != null) {
                try { connection.close(); } catch (SQLException ignored) {}
            }
        }
        return homes;
    }

    @Override
    public void insert(Home home) throws DaoException {
        Connection connection = null;
        PreparedStatement stmt = null;

        try {
            connection = this.source.getConnection();
            stmt = connection.prepareStatement(STMT_INSERT_HOME);
            stmt.setString(1, home.getOwnerUuid().toString());
            stmt.setString(2, home.getName());
            stmt.setString(3, home.getWorldUuid().toString());
            stmt.setDouble(4, home.getX());
            stmt.setDouble(5, home.getY());
            stmt.setDouble(6, home.getZ());
            stmt.execute();

        } catch (SQLException exception) {
            throw new DaoException(exception);
        } finally {
            if (stmt != null) {
                try { stmt.close(); } catch (SQLException ignored) {}
            }
            if (connection != null) {
                try { connection.close(); } catch (SQLException ignored) {}
            }
        }
    }

    @Override
    public void update(Home home) throws DaoException {
        Connection connection = null;
        PreparedStatement stmt = null;

        try {
            connection = this.source.getConnection();
            stmt = connection.prepareStatement(STMT_UPDATE_HOME);
            stmt.setString(1, home.getWorldUuid().toString());
            stmt.setDouble(2, home.getX());
            stmt.setDouble(3, home.getY());
            stmt.setDouble(4, home.getZ());
            stmt.setString(5, home.getOwnerUuid().toString());
            stmt.setString(6, home.getName());
            stmt.execute();

        } catch (SQLException exception) {
            throw new DaoException(exception);
        } finally {
            if (stmt != null) {
                try { stmt.close(); } catch (SQLException ignored) {}
            }
            if (connection != null) {
                try { connection.close(); } catch (SQLException ignored) {}
            }
        }
    }

    @Override
    public void delete(Home home) throws DaoException {
        Connection connection = null;
        PreparedStatement stmt = null;

        try {
            connection = this.source.getConnection();
            stmt = connection.prepareStatement(STMT_DELETE_HOME);
            stmt.setString(1, home.getOwnerUuid().toString());
            stmt.setString(2, home.getName());
            stmt.execute();

        } catch (SQLException exception) {
            throw new DaoException(exception);
        } finally {
            if (stmt != null) {
                try { stmt.close(); } catch (SQLException ignored) {}
            }
            if (connection != null) {
                try { connection.close(); } catch (SQLException ignored) {}
            }
        }
    }

}
