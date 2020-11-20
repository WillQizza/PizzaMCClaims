package io.github.willqi.pizzamc.claims.api.homes.exceptions;

/**
 * Exception used when a method is trying to be used before player homes are laoded.
 */
public class HomesNotLoadedException extends RuntimeException {

    public HomesNotLoadedException(String message) {
        super(message);
    }
}
