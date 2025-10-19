package com.docuflow.exception;

/**
 * Custom exception for when a document is not found
 * in either MySQL (metadata) or MongoDB (content).
 */
public class DocumentNotFoundException extends RuntimeException {

    public DocumentNotFoundException(String message) {
        super(message);
    }

    public DocumentNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
