package io.github.willqi.pizzamc.claims.api.users;

import io.github.willqi.pizzamc.claims.api.exceptions.DaoException;
import io.github.willqi.pizzamc.claims.api.users.dao.UsersDao;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * Contrary to the other managers, the UsersManager does not use a cache
 * because it is is not constantly called unlike the other managers.
 */
public class UsersManager {

    private final UsersDao usersDao;

    public UsersManager(UsersDao usersDao) {
        this.usersDao = usersDao;
    }

    /**
     * Fetch the stored user record of a uuid from the UsersDao
     * @param uuid
     * @return an empty optional if there is no record of the player yet or a user if one does exist.
     */
    public CompletableFuture<Optional<User>> fetchUser(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return this.usersDao.getUserByUuid(uuid);
            } catch (DaoException exception) {
                throw new CompletionException(exception);
            }
        });
    }

    /**
     * Fetch the stored user record of a name from the UsersDao
     * @param name
     * @return an empty optional if there is no record of the player yet or a user if one does exist.
     */
    public CompletableFuture<Optional<User>> fetchUser(String name) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return this.usersDao.getUserByName(name);
            } catch (DaoException exception) {
                throw new CompletionException(exception);
            }
        });
    }

    /**
     * Save a user record to the UsersDao
     * @param user
     * @return a CompletableFuture that resolves after saving
     */
    public CompletableFuture<Void> save(User user) {
        return this.fetchUser(user.getUuid()).thenAcceptAsync(results -> {
            try {
                if (results.isPresent()) {
                    // Ensure we only update if needed
                    if (!user.getName().equals(results.get().getName())) {
                        this.usersDao.update(user);
                    }
                } else {
                    this.usersDao.insert(user);
                }
            } catch (DaoException exception) {
                throw new CompletionException(exception);
            }
        });
    }

}
