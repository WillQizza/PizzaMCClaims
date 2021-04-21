package io.github.willqi.pizzamc.claims.api.daosources;

import io.github.willqi.pizzamc.claims.api.claims.dao.ClaimsDao;
import io.github.willqi.pizzamc.claims.api.claims.dao.ClaimsHelperDao;
import io.github.willqi.pizzamc.claims.api.homes.dao.HomesDao;
import io.github.willqi.pizzamc.claims.api.users.dao.UsersDao;

public interface DaoSource {

    ClaimsDao getClaimsDao();
    ClaimsHelperDao getClaimsHelperDao();
    HomesDao getHomesDao();
    UsersDao getUsersDao();

    void cleanUp();

}
