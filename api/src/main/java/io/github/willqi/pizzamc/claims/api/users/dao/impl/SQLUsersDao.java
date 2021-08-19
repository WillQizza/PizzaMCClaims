package io.github.willqi.pizzamc.claims.api.users.dao.impl;

import com.zaxxer.hikari.HikariDataSource;
import io.github.willqi.pizzamc.claims.api.exceptions.DaoException;
import io.github.willqi.pizzamc.claims.api.users.User;
import io.github.willqi.pizzamc.claims.api.users.dao.UsersDao;

import java.sql.*;
import java.util.Optional;
import java.util.UUID;

public class SQLUsersDao implements UsersDao {

    private static final String STMT_CREATE_USERS_TABLE = "CREATE TABLE IF NOT EXISTS users (" +
            "uuid VARCHAR(36) NOT NULL," +
            "name VARCHAR(16) NOT NULL," +
            "PRIMARY KEY(uuid)" +
            ");";

    private static final String STMT_GET_USER_BY_NAME = "SELECT uuid FROM users WHERE name=UPPER(?);";
    private static final String STMT_GET_USER_BY_UUID = "SELECT UPPER(name) FROM users WHERE uuid=?";
    private static final String STMT_INSERT_USER = "INSERT INTO users (uuid, name) VALUES (?, UPPER(?));";
    private static final String STMT_UPDATE_USER = "UPDATE users SET name=UPPER(?) WHERE uuid=?;";
    private static final String STMT_DELETE_USER = "DELETE FROM users WHERE uuid=?";

    private final HikariDataSource source;

    public SQLUsersDao(HikariDataSource source) throws SQLException {
        this.source = source;
        Connection connection = null;
        Statement stmt = null;
        try {
            connection = this.source.getConnection();
            stmt = connection.createStatement();
            stmt.execute(STMT_CREATE_USERS_TABLE);
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
    public Optional<User> getUserByName(String name) throws DaoException {
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet results = null;
        try {
            connection = this.source.getConnection();
            stmt = connection.prepareStatement(STMT_GET_USER_BY_NAME);
            stmt.setString(1, name);
            results = stmt.executeQuery();

            if (results.next()) {
                UUID uuid = UUID.fromString(results.getString("uuid"));
                User user = new User(uuid, name);
                return Optional.of(user);
            } else {
                return Optional.empty();
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
    }

    @Override
    public Optional<User> getUserByUuid(UUID uuid) throws DaoException {
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet results = null;
        try {
            connection = this.source.getConnection();
            stmt = connection.prepareStatement(STMT_GET_USER_BY_UUID);
            stmt.setString(1, uuid.toString());
            results = stmt.executeQuery();

            if (results.next()) {
                String name = results.getString("name");
                User user = new User(uuid, name);
                return Optional.of(user);
            } else {
                return Optional.empty();
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
    }

    @Override
    public void insert(User user) throws DaoException {
        Connection connection = null;
        PreparedStatement stmt = null;
        try {
            connection = this.source.getConnection();
            stmt = connection.prepareStatement(STMT_INSERT_USER);
            stmt.setString(1, user.getUUID().toString());
            stmt.setString(2, user.getName());
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
    public void update(User user) throws DaoException {
        Connection connection = null;
        PreparedStatement stmt = null;
        try {
            connection = this.source.getConnection();
            stmt = connection.prepareStatement(STMT_UPDATE_USER);
            stmt.setString(1, user.getName());
            stmt.setString(2, user.getUUID().toString());
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
    public void delete(User user) throws DaoException {
        Connection connection = null;
        PreparedStatement stmt = null;
        try {
            connection = this.source.getConnection();
            stmt = connection.prepareStatement(STMT_DELETE_USER);
            stmt.setString(1, user.getUUID().toString());
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
