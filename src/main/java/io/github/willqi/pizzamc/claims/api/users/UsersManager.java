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

    public CompletableFuture<Optional<User>> fetchUser(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return this.usersDao.getUserByUuid(uuid);
            } catch (DaoException exception) {
                throw new CompletionException(exception);
            }
        });
    }

    public CompletableFuture<Optional<User>> fetchUser(String name) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return this.usersDao.getUserByName(name);
            } catch (DaoException exception) {
                throw new CompletionException(exception);
            }
        });
    }

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
