package io.github.willqi.pizzamc.claims.api.claims;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.github.willqi.pizzamc.claims.api.claims.dao.ClaimsDao;
import io.github.willqi.pizzamc.claims.api.claims.dao.ClaimsHelperDao;
import io.github.willqi.pizzamc.claims.api.exceptions.DaoException;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class ClaimsManagerTest {

    private static final UUID NULL_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");
    private static final ChunkCoordinates DEFAULT_COORDINATES = new ChunkCoordinates(NULL_UUID, 0, 0);

    //
    // Claim tests
    //

    @Test
    public void fetchClaimShouldQueryDaoWithNoCacheData() {
        Claim daoClaim = new Claim(DEFAULT_COORDINATES, 0);

        TestClaimsDao mockClaimsDao = spy(new TestClaimsDao(){

            @Override
            public Optional<Claim> getClaimByLocation(ChunkCoordinates location) {
                return Optional.of(daoClaim);
            }

        });

        ClaimsManager claimsManager = new ClaimsManager(mockClaimsDao, new TestClaimsHelperDao());

        try {
            claimsManager.fetchClaim(daoClaim.getCoordinates()).get();
            claimsManager.fetchClaim(daoClaim.getCoordinates()).get();
        } catch (InterruptedException | ExecutionException exception) {
            throw new AssertionError("fetchClaim threw an error somehow", exception);
        }

        verify(mockClaimsDao, times(1)).getClaimByLocation(daoClaim.getCoordinates());
    }

    @Test
    public void fetchAndGetClaimShouldRetrieveCacheDataWhenAvailable() throws DaoException {
        Claim daoClaim = new Claim(DEFAULT_COORDINATES, 0);

        ClaimsDao mockClaimsDao = spy(new TestClaimsDao(){

            @Override
            public Optional<Claim> getClaimByLocation(ChunkCoordinates location) {
                return Optional.of(daoClaim);
            }

        });

        ClaimsManager claimsManager = new ClaimsManager(mockClaimsDao, new TestClaimsHelperDao());

        ChunkCoordinates coordinates = new ChunkCoordinates(NULL_UUID, 0, 0);
        try {
            claimsManager.fetchClaim(coordinates).get();

            // Ensure fetchClaim retrieves data from cache when possible
            assertEquals(claimsManager.fetchClaim(coordinates).get(), daoClaim);
        } catch (InterruptedException | ExecutionException exception) {
            throw new AssertionError("fetchClaim threw an error somehow", exception);
        }

        // Ensure cache has data
        assertEquals(claimsManager.getClaim(coordinates), Optional.of(daoClaim));
        verify(mockClaimsDao, times(1)).getClaimByLocation(coordinates);
    }

    @Test
    public void saveClaimShouldNotSaveEmptyClaims() {
        Claim emptyClaim = new Claim(DEFAULT_COORDINATES, 0);

        TestClaimsDao mockClaimsDao = spy(new TestClaimsDao(){

            @Override
            public Optional<Claim> getClaimByLocation(ChunkCoordinates location) {
                return Optional.of(new Claim(location, 0));
            }

        });

        ClaimsManager claimsManager = new ClaimsManager(mockClaimsDao, new TestClaimsHelperDao());
        try {
            claimsManager.saveClaim(emptyClaim).get();
        } catch (InterruptedException | ExecutionException exception) {
            throw new AssertionError("saveClaim threw an error somehow", exception);
        }

        verify(mockClaimsDao, times(0)).update(emptyClaim);
        verify(mockClaimsDao, times(0)).insert(emptyClaim);

    }

    @Test
    public void savingEmptyClaimShouldOverwriteExistingClaims() throws DaoException {
        Claim daoClaim = new Claim(DEFAULT_COORDINATES, 1);
        ClaimsDao mockClaimsDao = spy(new TestClaimsDao() {

            @Override
            public Optional<Claim> getClaimByLocation(ChunkCoordinates location) {
                return Optional.of(new Claim(location, 0));
            }

            @Override
            public void update(Claim claim) {
                assertEquals(0, claim.getFlags());  // The empty claim should be saved
            }

        });

        ClaimsManager claimsManager = new ClaimsManager(mockClaimsDao, new TestClaimsHelperDao());
        try {
            claimsManager.saveClaim(daoClaim).get();
            daoClaim.setFlags(0);
            claimsManager.saveClaim(daoClaim).get();
        } catch (InterruptedException | ExecutionException exception) {
            throw new AssertionError("saveClaim threw an error somehow", exception);
        }
        verify(mockClaimsDao, times(1)).update(daoClaim);
    }

    @Test
    public void saveClaimShouldUpdateIfExistingClaimProvided() throws DaoException {
        Claim daoClaim = new Claim(DEFAULT_COORDINATES, NULL_UUID, 0);

        ClaimsDao mockClaimsDao = spy(new TestClaimsDao() {

            @Override
            public Optional<Claim> getClaimByLocation(ChunkCoordinates location) {
                return Optional.of(new Claim(location, 0));
            }

        });

        ClaimsManager claimsManager = new ClaimsManager(mockClaimsDao, new TestClaimsHelperDao());

        try {
            claimsManager.saveClaim(daoClaim).get();    // Insert claim

            daoClaim.setOwner(UUID.fromString("10000000-0000-0000-0000-000000000000"));
            claimsManager.saveClaim(daoClaim).get();    // This should update the claim
        } catch (InterruptedException | ExecutionException exception) {
            throw new AssertionError("saveClaim threw an error somehow", exception);
        }
        verify(mockClaimsDao, times(1)).update(daoClaim);
    }

    @Test
    public void saveClaimShouldInsertIfNoExistingClaimFound() throws DaoException {
        Claim daoClaim = new Claim(DEFAULT_COORDINATES, 1);

        ClaimsDao mockClaimsDao = spy(new TestClaimsDao() {

            @Override
            public Optional<Claim> getClaimByLocation(ChunkCoordinates location) {
                return Optional.of(new Claim(location, 0));
            }

        });

        ClaimsManager claimsManager = new ClaimsManager(mockClaimsDao, new TestClaimsHelperDao());
        try {
            claimsManager.saveClaim(daoClaim).get();
        } catch (InterruptedException | ExecutionException exception) {
            throw new AssertionError("saveClaim threw an error somehow", exception);
        }
        verify(mockClaimsDao, times(1)).insert(daoClaim);
    }


    @Test
    public void fetchAndGetClaimShouldReturnAClone() {
        Claim daoClaim = new Claim(DEFAULT_COORDINATES, 1);

        ClaimsManager claimsManager = new ClaimsManager(new TestClaimsDao() {

            @Override
            public Optional<Claim> getClaimByLocation(ChunkCoordinates location) {
                return Optional.of(new Claim(location, 0));
            }

        }, new TestClaimsHelperDao());

        // Check flags of fetched claim to ensure it is different
        try {
            claimsManager.saveClaim(daoClaim).get();
            daoClaim.setFlags(2);
            Claim fetchedClaim = claimsManager.fetchClaim(daoClaim.getCoordinates()).get();
            assertNotEquals(daoClaim.getFlags(), fetchedClaim.getFlags());

            // Ensure that the fetched claim returns a cloned claim and that it does not modify the original
            fetchedClaim.setFlags(3);
            assertNotEquals(fetchedClaim.getFlags(), claimsManager.fetchClaim(daoClaim.getCoordinates()).get().getFlags());
        } catch (InterruptedException | ExecutionException exception) {
            throw new AssertionError("fetchClaim threw an error somehow", exception);
        }

        // Check flags of cached claim to ensure they are different
        Optional<Claim> cachedClaim = claimsManager.getClaim(daoClaim.getCoordinates());
        if (!cachedClaim.isPresent()) {
            throw new AssertionError("Cached claim was not present for some reason");
        }
        assertNotEquals(daoClaim.getFlags(), cachedClaim.get().getFlags());

        // The cached claim should be a duplicate of the original claim
        cachedClaim.get().setFlags(daoClaim.getFlags());
        assertNotEquals(cachedClaim.get().getFlags(), claimsManager.getClaim(daoClaim.getCoordinates()).get().getFlags());

    }

    @Test
    public void fetchAndGetClaimCountShouldReturnClaimCountOfUuid() throws DaoException {
        TestClaimsDao mockClaimsDao = spy(new TestClaimsDao(){
            @Override
            public int getClaimCountOfUuid(UUID uuid) {
                return 1;
            }
        });
        ClaimsManager claimsManager = new ClaimsManager(mockClaimsDao, new TestClaimsHelperDao());
        try {
            int total = claimsManager.fetchClaimCount(NULL_UUID).get();
            assertEquals(total, 1);
        } catch (InterruptedException | ExecutionException exception) {
            throw new AssertionError("fetchClaimCount threw an exception somehow.", exception);
        }
        verify(mockClaimsDao, times(1)).getClaimCountOfUuid(NULL_UUID);

        Optional<Integer> result = claimsManager.getClaimCount(NULL_UUID);
        if (result.isPresent()) {
            assertEquals(result.get(), 1);
        } else {
            throw new AssertionError("Claim count was not cached somehow.");
        }
    }

    @Test
    public void addingClaimOwnerShouldIncrementClaimCountOfUuid() {
        ClaimsManager claimsManager = new ClaimsManager(new TestClaimsDao(), new TestClaimsHelperDao());
        try {
            claimsManager.fetchClaimCount(NULL_UUID).get();

            // Add a new claim that we own
            Claim ourClaim = new Claim(new ChunkCoordinates(NULL_UUID, 0, 0), NULL_UUID, 0);
            claimsManager.saveClaim(ourClaim).get();

            // Steal someone else's claim
            Claim tempNotOurClaim = new Claim(new ChunkCoordinates(NULL_UUID, 1, 1), UUID.fromString("10000000-0000-0000-0000-000000000000"), 0);
            claimsManager.saveClaim(tempNotOurClaim).get();
            tempNotOurClaim.setOwner(NULL_UUID);
            claimsManager.saveClaim(tempNotOurClaim).get();

        } catch (InterruptedException | ExecutionException exception) {
            throw new AssertionError("saveClaim threw an exception somehow.", exception);
        }

        Optional<Integer> result = claimsManager.getClaimCount(NULL_UUID);
        if (result.isPresent()) {
            assertEquals(result.get(), 2);
        } else {
            throw new AssertionError("Claim count was not cached somehow.");
        }
    }

    @Test
    public void removingClaimOwnerShouldDecrementClaimCountOfUuid() {
        ClaimsManager claimsManager = new ClaimsManager(new TestClaimsDao(), new TestClaimsHelperDao());
        try {
            claimsManager.fetchClaimCount(NULL_UUID).get();

            // Remove a claim we own.
            Claim ourClaim = new Claim(new ChunkCoordinates(NULL_UUID, 0, 0), NULL_UUID, 0);
            claimsManager.saveClaim(ourClaim).get();
            ourClaim.setOwner(null);
            claimsManager.saveClaim(ourClaim).get();

        } catch (InterruptedException | ExecutionException exception) {
            throw new AssertionError("saveClaim threw an exception somehow.", exception);
        }

        Optional<Integer> result = claimsManager.getClaimCount(NULL_UUID);
        if (result.isPresent()) {
            assertEquals(result.get(), 0);
        } else {
            throw new AssertionError("Claim count was not cached somehow.");
        }
    }

    @Test
    public void updatingClaimFlagsShouldNotChangeClaimCountOfUuid() {
        ClaimsManager claimsManager = new ClaimsManager(new TestClaimsDao(), new TestClaimsHelperDao());
        try {
            claimsManager.fetchClaimCount(NULL_UUID).get();

            // Remove a claim we own.
            Claim ourClaim = new Claim(new ChunkCoordinates(NULL_UUID, 0, 0), NULL_UUID, 0);
            claimsManager.saveClaim(ourClaim).get();
            ourClaim.setFlags(5);
            claimsManager.saveClaim(ourClaim).get();

        } catch (InterruptedException | ExecutionException exception) {
            throw new AssertionError("saveClaim threw an exception somehow.", exception);
        }

        Optional<Integer> result = claimsManager.getClaimCount(NULL_UUID);
        if (result.isPresent()) {
            assertEquals(result.get(), 1);
        } else {
            throw new AssertionError("Claim count was not cached somehow.");
        }
    }



    //
    //  ClaimHelper tests
    //

    @Test
    public void fetchClaimHelpersShouldQueryDaoWithNoCacheData() throws DaoException {
        ClaimsHelperDao mockHelpersDao = spy(new TestClaimsHelperDao());
        ClaimsManager claimsManager = new ClaimsManager(new TestClaimsDao(), mockHelpersDao);
        ChunkCoordinates coordinates = new ChunkCoordinates(NULL_UUID, 0, 0);

        try {
            claimsManager.fetchClaimHelpers(coordinates).get();
        } catch (InterruptedException | ExecutionException exception) {
            throw new AssertionError("fetchClaimHelpers threw an error somehow", exception);
        }

        verify(mockHelpersDao, times(1)).getClaimHelpersByLocation(coordinates);
    }

    @Test
    public void fetchAndGetClaimHelpersShouldRetrieveCacheDataWhenAvailable() throws DaoException {
        ChunkCoordinates coordinates = new ChunkCoordinates(NULL_UUID, 0, 0);
        ClaimHelper daoHelper = new ClaimHelper(NULL_UUID, ClaimHelper.Permission.BUILD.getValue());

        ClaimsHelperDao mockClaimsHelpersDao = spy(new TestClaimsHelperDao(){

            @Override
            public Set<ClaimHelper> getClaimHelpersByLocation(ChunkCoordinates location) {
                Set<ClaimHelper> helpers = new HashSet<>();
                helpers.add(daoHelper);
                return helpers;
            }

        });

        ClaimsManager claimsManager = new ClaimsManager(new TestClaimsDao(), mockClaimsHelpersDao);
        Set<ClaimHelper> comparisonSet = new HashSet<>();
        comparisonSet.add(daoHelper);

        try {
            claimsManager.fetchClaimHelpers(coordinates).get();
            assertEquals(comparisonSet, claimsManager.fetchClaimHelpers(coordinates).get());    // Should fetch from cache and get same contents
        } catch (InterruptedException | ExecutionException exception) {
            throw new AssertionError("fetchClaimHelpers threw an error somehow", exception);
        }
        assertEquals(claimsManager.getClaimHelpers(coordinates), Optional.of(comparisonSet));   // The cache should have the same data
        verify(mockClaimsHelpersDao, times(1)).getClaimHelpersByLocation(coordinates);  // Ensure it did not query the dao twice
    }

    @Test
    public void fetchAndGetClaimHelpersShouldReturnClones() {
        ChunkCoordinates coordinates = new ChunkCoordinates(NULL_UUID, 0, 0);
        ClaimHelper helper = new ClaimHelper(NULL_UUID, ClaimHelper.Permission.BUILD.getValue());

        ClaimsManager claimsManager = new ClaimsManager(new TestClaimsDao(), new TestClaimsHelperDao());

        // Check fetched claim helper's permission
        try {
            claimsManager.saveClaimHelper(coordinates, helper).get();
            helper.setPermissions(2);
            Set<ClaimHelper> fetchedHelpers = claimsManager.fetchClaimHelpers(coordinates).get();
            assertNotEquals(helper.getPermissions(), ((ClaimHelper)(fetchedHelpers.toArray())[0]).getPermissions());
        } catch (InterruptedException | ExecutionException exception) {
            throw new AssertionError("This test threw an error somehow", exception);
        }

        // Check cached claim helper's permission
        Optional<ClaimHelper> cachedClaimHelper = claimsManager.getClaimHelper(coordinates, helper.getUuid());
        if (!cachedClaimHelper.isPresent()) {
            throw new AssertionError("Cached claim helper does not exist for some reason");
        }
        assertNotEquals(helper.getPermissions(), cachedClaimHelper.get().getPermissions());

        // The cached claim helper should be a clone of the original stored helper
        cachedClaimHelper.get().setPermissions(helper.getPermissions());
        assertNotEquals(cachedClaimHelper.get().getPermissions(), claimsManager.getClaimHelper(coordinates, helper.getUuid()).get().getPermissions());
    }

    @Test
    public void saveClaimHelpersShouldUpdateIfExistingHelperProvided() throws DaoException {
        ChunkCoordinates coordinates = new ChunkCoordinates(NULL_UUID, 0, 0);
        ClaimHelper helper = new ClaimHelper(NULL_UUID, ClaimHelper.Permission.BUILD.getValue());

        ClaimsHelperDao mockClaimsHelperDao = spy(new TestClaimsHelperDao());

        ClaimsManager claimsManager = new ClaimsManager(new TestClaimsDao(), mockClaimsHelperDao);
        try {
            claimsManager.saveClaimHelper(coordinates, helper).get();
            helper.addPermission(ClaimHelper.Permission.INTERACT);
            claimsManager.saveClaimHelper(coordinates, helper).get();
        } catch (InterruptedException | ExecutionException exception) {
            throw new AssertionError("saveClaimHelper threw an error somehow", exception);
        }
        verify(mockClaimsHelperDao, times(1)).update(coordinates, helper);
    }

    @Test
    public void saveClaimHelpersShouldInsertIfNoExistingHelperFound() throws DaoException {
        ChunkCoordinates coordinates = new ChunkCoordinates(NULL_UUID, 0, 0);
        ClaimHelper helper = new ClaimHelper(NULL_UUID, ClaimHelper.Permission.BUILD.getValue());

        ClaimsHelperDao mockClaimsHelperDao = spy(new TestClaimsHelperDao());

        ClaimsManager claimsManager = new ClaimsManager(new TestClaimsDao(), mockClaimsHelperDao);
        try {
            claimsManager.saveClaimHelper(coordinates, helper).get();
        } catch (InterruptedException | ExecutionException exception) {
            throw new AssertionError("saveClaimHelper threw an error somehow", exception);
        }
        verify(mockClaimsHelperDao, times(1)).insert(coordinates, helper);
    }

    @Test
    public void saveClaimHelperShouldNotInsertEmptyHelper() throws DaoException {
        ChunkCoordinates coordinates = new ChunkCoordinates(NULL_UUID, 0, 0);
        ClaimHelper helper = new ClaimHelper(NULL_UUID, 0);

        ClaimsHelperDao mockClaimsHelperDao = spy(new TestClaimsHelperDao());

        ClaimsManager claimsManager = new ClaimsManager(new TestClaimsDao(), mockClaimsHelperDao);
        try {
            claimsManager.saveClaimHelper(coordinates, helper).get();
        } catch (InterruptedException | ExecutionException exception) {
            throw new AssertionError("saveClaimHelper threw an error somehow", exception);
        }
        verify(mockClaimsHelperDao, times(0)).insert(coordinates, helper);
        verify(mockClaimsHelperDao, times(0)).update(coordinates, helper);
    }

    @Test
    public void deletingAClaimShouldDeleteAllHelpers() {
        ChunkCoordinates coordinates = new ChunkCoordinates(NULL_UUID, 0, 0);
        ClaimHelper helper = new ClaimHelper(NULL_UUID, 0);

        ClaimsManager claimsManager = new ClaimsManager(new TestClaimsDao(), new TestClaimsHelperDao());
        try {
            claimsManager.saveClaimHelper(coordinates, helper).get();
            claimsManager.deleteClaim(new Claim(coordinates, 0)).get();
        } catch (InterruptedException | ExecutionException exception) {
            throw new AssertionError("This test threw an exception somehow", exception);
        }
        Optional<Set<ClaimHelper>> helpers = claimsManager.getClaimHelpers(coordinates);
        if (!helpers.isPresent()) {
            throw new AssertionError("Cached helpers was not present");
        }
        assertEquals(helpers.get().size(), 0);

    }





    //
    // Utility classes
    //

    private static class TestClaimsDao implements ClaimsDao {

        @Override
        public Optional<Claim> getClaimByLocation(ChunkCoordinates location) {
            return Optional.empty();
        }

        @Override
        public int getClaimCountOfUuid(UUID uuid) throws DaoException {
            return 0;
        }

        @Override
        public void delete(Claim claim) {

        }

        @Override
        public void update(Claim claim) {

        }

        @Override
        public void insert(Claim claim) {

        }
    }

    private static class TestClaimsHelperDao implements ClaimsHelperDao {

        @Override
        public Set<ClaimHelper> getClaimHelpersByLocation(ChunkCoordinates location) {
            return new HashSet<>();
        }

        @Override
        public void delete(ChunkCoordinates claimCoords, ClaimHelper helper) {

        }

        @Override
        public void update(ChunkCoordinates claimCoords, ClaimHelper helper) {

        }

        @Override
        public void insert(ChunkCoordinates claimCoords, ClaimHelper helper) {

        }

    }


}
