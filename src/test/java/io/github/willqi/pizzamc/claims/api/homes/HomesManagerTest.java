package io.github.willqi.pizzamc.claims.api.homes;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.github.willqi.pizzamc.claims.api.exceptions.DaoException;
import io.github.willqi.pizzamc.claims.api.homes.dao.HomesDao;
import io.github.willqi.pizzamc.claims.api.exceptions.InvalidHomeNameException;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.ExecutionException;

public class HomesManagerTest {

    private static final UUID NULL_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    @Test
    public void fetchHomesShouldQueryDaoWithNoCacheData() throws DaoException {
        HomesDao mockHomesDao = spy(new TestHomesDao());
        HomesManager homesManager = new HomesManager(mockHomesDao);

        try {
            homesManager.fetchHomes(NULL_UUID).get();
            homesManager.fetchHomes(NULL_UUID).get();
        } catch (ExecutionException | InterruptedException exception) {
            throw new AssertionError("fetchHomes threw a exception somehow.", exception);
        }
        verify(mockHomesDao, times(1)).getHomesByOwner(NULL_UUID);
    }

    @Test
    public void fetchAndGetHomesShouldRetrieveCacheDataWhenAvailable() {
        String homeName = "Test Name";
        Home daoHome = createHome(NULL_UUID, homeName);

        HomesDao mockHomeDao = spy(new TestHomesDao(){
            @Override
            public Set<Home> getHomesByOwner(UUID uuid) {
                Set<Home> homes = new HashSet<>();
                homes.add(daoHome);
                return homes;
            }
        });
        HomesManager homesManager = new HomesManager(mockHomeDao);
        Map<String, Home> homes;
        try {
            homesManager.fetchHomes(NULL_UUID).get();
            homes = homesManager.fetchHomes(NULL_UUID).get();
        } catch (ExecutionException | InterruptedException exception) {
            throw new AssertionError("fetchHomes threw an exception somehow", exception);
        }
        assertEquals(1, homes.size());

        Optional<Home> cachedHome = homesManager.getHome(NULL_UUID, homeName);
        if (cachedHome.isPresent()) {
            assertEquals(daoHome, cachedHome.get());
        } else {
            throw new AssertionError("Could not find cached home for some reason");
        }
    }

    @Test
    public void saveShouldInsertIfNoHomeFound() throws DaoException {
        HomesDao mockHomesDao = spy(new TestHomesDao());
        HomesManager homesManager = new HomesManager(mockHomesDao);

        Home home = createHome(NULL_UUID, "Home Name");
        Home home2 = createHome(UUID.fromString("10000000-0000-0000-0000-000000000000"), "Home Name");
        try {
            homesManager.save(home).get();
            homesManager.save(home2).get();
        } catch (ExecutionException | InterruptedException exception) {
            throw new AssertionError("save threw an exception somehow", exception);
        }
        verify(mockHomesDao, times(1)).insert(home);
        verify(mockHomesDao, times(1)).insert(home2);
    }

    @Test
    public void saveShouldUpdateIfHomeFound() throws DaoException {
        HomesDao mockHomesDao = spy(new TestHomesDao());
        HomesManager homesManager = new HomesManager(mockHomesDao);

        Home home = createHome(NULL_UUID, "Test Home");
        try {
            homesManager.save(home).get();
            home.setX(1);
            homesManager.save(home).get();
        } catch (ExecutionException | InterruptedException exception) {
            throw new AssertionError("save threw an exception somehow", exception);
        }
        verify(mockHomesDao, times(1)).update(home);
    }

    @Test
    public void fetchAndGetHomesShouldReturnClones() {
        HomesManager homesManager = new HomesManager(new TestHomesDao());
        Home comparisonHome = createHome(NULL_UUID, "Test home");

        // Check fetch
        try {
            homesManager.save(comparisonHome).get();
            comparisonHome.setX(1);
            Map<String, Home> homes = homesManager.fetchHomes(NULL_UUID).get(); // NULL_UUID is also the UUID of the owner
            assertEquals(0, homes.get(comparisonHome.getName()).getX());

            // Change the x of the returned map too
            homes.get(comparisonHome.getName()).setX(1);
            homes = homesManager.fetchHomes(NULL_UUID).get();
            assertEquals(0, homes.get(comparisonHome.getName()).getX());
        } catch (ExecutionException | InterruptedException exception) {
            throw new AssertionError("The test threw an exception somehow", exception);
        }

        // Check cache
        Optional<Map<String, Home>> homes = homesManager.getHomes(NULL_UUID);
        if (!homes.isPresent() || !homes.get().containsKey(comparisonHome.getName())) {
            throw new AssertionError("Failed to find home in cache");
        }
        assertEquals(0, homes.get().get(comparisonHome.getName()).getX());  // Ensure that the cache was not modified

        homes.get().get(comparisonHome.getName()).setX(1);
        homes = homesManager.getHomes(NULL_UUID);
        if (!homes.isPresent() || !homes.get().containsKey(comparisonHome.getName())) {
            throw new AssertionError("Failed to find home in cache");
        }
        assertEquals(0, homes.get().get(comparisonHome.getName()).getX());
    }



    private static Home createHome(UUID ownerUuid, String name) {
        try {
            return new Home(ownerUuid, name, NULL_UUID, 0, 0, 0);
        } catch (InvalidHomeNameException exception) {
            throw new AssertionError("Test home name was invalid.");
        }
    }

    private static class TestHomesDao implements HomesDao {

        @Override
        public Set<Home> getHomesByOwner(UUID uuid) {
            return new HashSet<>();
        }

        @Override
        public void insert(Home home) {

        }

        @Override
        public void update(Home home) {

        }

        @Override
        public void delete(Home home) {

        }

    }

}
