package io.github.willqi.pizzamc.claims.api.claims.dao.impl;

import com.zaxxer.hikari.HikariDataSource;
import io.github.willqi.pizzamc.claims.api.claims.ChunkCoordinates;
import io.github.willqi.pizzamc.claims.api.claims.ClaimHelper;
import io.github.willqi.pizzamc.claims.api.claims.dao.ClaimHelpersDao;
import io.github.willqi.pizzamc.claims.api.exceptions.DaoException;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class SQLClaimHelpersDao implements ClaimHelpersDao {

    private static final String STMT_CREATE_HELPERS_TABLE = "CREATE TABLE IF NOT EXISTS claim_helpers (" +
            "world_uuid VARCHAR(36) NOT NULL," +
            "x INT NOT NULL," +
            "z INT NOT NULL," +
            "uuid VARCHAR(36) NOT NULL," +
            "permissions INT NOT NULL," +
            "FOREIGN KEY(world_uuid, x, z) REFERENCES claims (world_uuid, x, z)" +
            ")";

    private static final String STMT_GET_HELPERS = "SELECT uuid, permissions FROM claim_helpers WHERE world_uuid=? AND x=? AND z=?";
    private static final String STMT_INSERT_HELPER = "INSERT INTO claim_helpers (world_uuid, x, z, uuid, permissions) VALUES (?, ?, ?, ?, ?)";
    private static final String STMT_UPDATE_HELPER = "UPDATE claim_helpers SET permissions=? WHERE world_uuid=? AND x=? AND z=? AND uuid=?";
    private static final String STMT_DELETE_HELPER = "DELETE FROM claim_helpers WHERE world_uuid=? AND x=? AND z=? AND uuid=?";

    private final HikariDataSource source;

    public SQLClaimHelpersDao(HikariDataSource source) throws SQLException {
        this.source = source;

        Connection connection = null;
        Statement stmt = null;

        try {
            connection = this.source.getConnection();
            stmt = connection.createStatement();
            stmt.execute(STMT_CREATE_HELPERS_TABLE);
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
    public Set<ClaimHelper> getClaimHelpersByLocation(ChunkCoordinates location) throws DaoException {
        Set<ClaimHelper> helpers = new HashSet<>();
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet results = null;

        try {
            connection = this.source.getConnection();
            stmt = connection.prepareStatement(STMT_GET_HELPERS);
            stmt.setString(1, location.getWorldUUID().toString());
            stmt.setInt(2, location.getX());
            stmt.setInt(3, location.getZ());
            results = stmt.executeQuery();

            while (results.next()) {
                UUID uuid = UUID.fromString(results.getString("uuid"));
                int permissions = results.getInt("permissions");
                ClaimHelper helper = new ClaimHelper(uuid, permissions);
                helpers.add(helper);
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

        return helpers;
    }

    @Override
    public void insert(ChunkCoordinates claimCoords, ClaimHelper helper) throws DaoException {
        Connection connection = null;
        PreparedStatement stmt = null;
        try {
            connection = this.source.getConnection();
            stmt = connection.prepareStatement(STMT_INSERT_HELPER);
            stmt.setString(1, claimCoords.getWorldUUID().toString());
            stmt.setInt(2, claimCoords.getX());
            stmt.setInt(3, claimCoords.getZ());
            stmt.setString(4, helper.getUuid().toString());
            stmt.setInt(5, helper.getPermissions());
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
    public void update(ChunkCoordinates claimCoords, ClaimHelper helper) throws DaoException {
        Connection connection = null;
        PreparedStatement stmt = null;
        try {
            connection = this.source.getConnection();
            stmt = connection.prepareStatement(STMT_UPDATE_HELPER);
            stmt.setInt(1, helper.getPermissions());
            stmt.setString(2, claimCoords.getWorldUUID().toString());
            stmt.setInt(3, claimCoords.getX());
            stmt.setInt(4, claimCoords.getZ());
            stmt.setString(5, helper.getUuid().toString());
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
    public void delete(ChunkCoordinates claimCoords, ClaimHelper helper) throws DaoException {
        Connection connection = null;
        PreparedStatement stmt = null;
        try {
            connection = this.source.getConnection();
            stmt = connection.prepareStatement(STMT_DELETE_HELPER);
            stmt.setString(1, claimCoords.getWorldUUID().toString());
            stmt.setInt(2, claimCoords.getX());
            stmt.setInt(3, claimCoords.getZ());
            stmt.setString(4, helper.getUuid().toString());
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
