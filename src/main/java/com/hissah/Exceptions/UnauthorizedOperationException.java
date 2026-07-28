package com.hissah.Exceptions;

/**
 * Raised when the authenticated user does not own or control a resource.
 */
public class UnauthorizedOperationException extends RuntimeException {

    public UnauthorizedOperationException(String message) {
        super(message);
    }
}
