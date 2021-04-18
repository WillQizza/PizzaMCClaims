package io.github.willqi.pizzamc.claims.api.daosources;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool;
import io.github.willqi.pizzamc.claims.api.claims.dao.ClaimsDao;
import io.github.willqi.pizzamc.claims.api.claims.dao.ClaimsHelperDao;
import io.github.willqi.pizzamc.claims.api.claims.dao.impl.SQLClaimsDao;
import io.github.willqi.pizzamc.claims.api.claims.dao.impl.SQLClaimsHelperDao;
import io.github.willqi.pizzamc.claims.api.exceptions.DaoException;
import io.github.willqi.pizzamc.claims.api.homes.dao.HomesDao;
import io.github.willqi.pizzamc.claims.api.homes.dao.impl.SQLHomesDao;

import java.sql.SQLException;

public class SQLDaoSource implements DaoSource {

    private ClaimsDao claimsDao;
    private ClaimsHelperDao claimsHelperDao;
    private HomesDao homesDao;

    private HikariDataSource source;

    public SQLDaoSource(String host, int port, String database, String username, String password) throws DaoException {
        HikariConfig dbConfig = new HikariConfig();
        dbConfig.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database);
        dbConfig.setUsername(username);
        dbConfig.setPassword(password);
        try {
            this.source = new HikariDataSource(dbConfig);
        } catch (HikariPool.PoolInitializationException exception) {
            throw new DaoException("Failed to initialize hikari source.", exception);
        }

        try {
            this.claimsDao = new SQLClaimsDao(this.source);
            this.claimsHelperDao = new SQLClaimsHelperDao(this.source);
            this.homesDao = new SQLHomesDao(this.source);
        } catch (SQLException exception) {
            throw new DaoException("Failed to create daos", exception);
        }
    }

    @Override
    public ClaimsDao getClaimsDao() {
        return this.claimsDao;
    }

    @Override
    public ClaimsHelperDao getClaimsHelperDao() {
        return this.claimsHelperDao;
    }

    @Override
    public HomesDao getHomesDao() {
        return this.homesDao;
    }

    @Override
    public void cleanUp() {
        this.source.close();
    }

}
