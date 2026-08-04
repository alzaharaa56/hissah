package com.hissah.Exceptions;

/**
 * Raised when a requested action breaks a Hissah workflow rule,
 * such as bidding after a deadline or awarding an already-awarded package.
 */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }

    public BusinessRuleException(String message, Throwable cause) {
        super(message, cause);
    }
}
