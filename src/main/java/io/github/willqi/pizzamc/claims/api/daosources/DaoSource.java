package io.github.willqi.pizzamc.claims.api.daosources;

import io.github.willqi.pizzamc.claims.api.claims.dao.ClaimsDao;
import io.github.willqi.pizzamc.claims.api.claims.dao.ClaimsHelperDao;
import io.github.willqi.pizzamc.claims.api.homes.dao.HomesDao;

public interface DaoSource {

    ClaimsDao getClaimsDao();
    ClaimsHelperDao getClaimsHelperDao();
    HomesDao getHomesDao();

    void cleanUp();

}
