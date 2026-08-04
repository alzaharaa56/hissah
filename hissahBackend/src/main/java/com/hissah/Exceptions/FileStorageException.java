package com.hissah.Exceptions;

/**
 * Raised for unsafe, invalid, or failed local file-storage operations.
 */
public class FileStorageException extends RuntimeException {

    public FileStorageException(String message) {
        super(message);
    }

    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
