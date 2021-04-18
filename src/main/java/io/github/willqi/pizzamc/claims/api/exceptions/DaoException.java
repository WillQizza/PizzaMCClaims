package io.github.willqi.pizzamc.claims.api.exceptions;

/**
 * The DaoException is to be used to wrap any database exceptions that occur in a Dao
 */
public class DaoException extends Exception {

    public DaoException(String message) {
        super(message);
    }

    public DaoException(Throwable throwable) {
        super(throwable);
    }

}
