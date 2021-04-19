package io.github.willqi.pizzamc.claims.api.claims.dao.impl;

import com.zaxxer.hikari.HikariDataSource;
import io.github.willqi.pizzamc.claims.api.claims.ChunkCoordinates;
import io.github.willqi.pizzamc.claims.api.claims.Claim;
import io.github.willqi.pizzamc.claims.api.claims.dao.ClaimsDao;
import io.github.willqi.pizzamc.claims.api.exceptions.DaoException;

import java.sql.*;
import java.util.Optional;
import java.util.UUID;

public class SQLClaimsDao implements ClaimsDao {

    private static final String STMT_CREATE_CLAIMS_TABLE = "CREATE TABLE IF NOT EXISTS claims (" +
            "worldUuid VARCHAR(36) NOT NULL," +
            "x INT NOT NULL," +
            "z INT NOT NULL," +
            "ownerUuid VARCHAR(36)," +
            "flags INT NOT NULL," +
            "PRIMARY KEY(worldUuid, x, z)" +
            ")";

    private static final String STMT_GET_CLAIM = "SELECT ownerUuid, flags FROM claims WHERE worldUuid=? AND x=? AND z=?";
    private static final String STMT_INSERT_CLAIM = "INSERT INTO claims (worldUuid, x, z, ownerUuid, flags) VALUES (?, ?, ?, ?, ?)";
    private static final String STMT_UPDATE_CLAIM = "UPDATE claims SET ownerUuid=?, flags=? WHERE worldUuid=? AND x=? AND z=?";
    private static final String STMT_DELETE_CLAIM = "DELETE FROM claims WHERE worldUuid=? AND x=? AND z=?";

    private final HikariDataSource source;

    public SQLClaimsDao(HikariDataSource source) throws SQLException {
        this.source = source;
        Connection connection = null;
        Statement stmt = null;

        try {
            connection = this.source.getConnection();
            stmt = connection.createStatement();
            stmt.execute(STMT_CREATE_CLAIMS_TABLE);
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
    public Optional<Claim> getClaimByLocation(ChunkCoordinates coordinates) throws DaoException {
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet results = null;

        Claim claim = null;

        try {
            connection = this.source.getConnection();
            stmt = connection.prepareStatement(STMT_GET_CLAIM);
            stmt.setString(1, coordinates.getWorldUuid().toString());
            stmt.setInt(2, coordinates.getX());
            stmt.setInt(3, coordinates.getZ());
            results = stmt.executeQuery();

            if (results.next()) {
                UUID worldUuid = UUID.fromString(results.getString("worldUuid"));
                int x = results.getInt("x");
                int z = results.getInt("z");
                int flags = results.getInt("flags");
                String ownerUuidStr = results.getString("ownerUuid");

                if (ownerUuidStr == null) {
                    claim = new Claim(worldUuid, x, z, flags);
                } else {
                    claim = new Claim(worldUuid, x, z, UUID.fromString(ownerUuidStr), flags);
                }
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
        return Optional.ofNullable(claim);
    }

    @Override
    public void insert(Claim claim) throws DaoException {
        Connection connection = null;
        PreparedStatement stmt = null;
        try {
            connection = this.source.getConnection();
            stmt = connection.prepareStatement(STMT_INSERT_CLAIM);
            stmt.setString(1, claim.getWorldUuid().toString());
            stmt.setInt(2, claim.getX());
            stmt.setInt(3, claim.getZ());
            Optional<UUID> claimOwner = claim.getOwner();
            if (claimOwner.isPresent()) {
                stmt.setString(4, claimOwner.get().toString());
            } else {
                stmt.setString(4, null);
            }
            stmt.setInt(5, claim.getFlags());
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
    public void update(Claim claim) throws DaoException {
        Connection connection = null;
        PreparedStatement stmt = null;
        try {
            connection = this.source.getConnection();
            stmt = connection.prepareStatement(STMT_UPDATE_CLAIM);
            Optional<UUID> claimOwner = claim.getOwner();
            if (claimOwner.isPresent()) {
                stmt.setString(1, claimOwner.get().toString());
            } else {
                stmt.setString(1, null);
            }
            stmt.setInt(2, claim.getFlags());
            stmt.setString(3, claim.getWorldUuid().toString());
            stmt.setInt(4, claim.getX());
            stmt.setInt(5, claim.getZ());
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
    public void delete(Claim claim) throws DaoException {
        Connection connection = null;
        PreparedStatement stmt = null;
        try {
            connection = this.source.getConnection();
            stmt = connection.prepareStatement(STMT_DELETE_CLAIM);
            stmt.setString(1, claim.getWorldUuid().toString());
            stmt.setInt(2, claim.getX());
            stmt.setInt(3, claim.getZ());
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
