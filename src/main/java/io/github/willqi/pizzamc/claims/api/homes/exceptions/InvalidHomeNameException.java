package io.github.willqi.pizzamc.claims.api.homes.exceptions;

/**
 * Exception used for invalid house names
 */
public class InvalidHomeNameException extends Exception {

    public InvalidHomeNameException(String message) {
        super(message);
    }
}
