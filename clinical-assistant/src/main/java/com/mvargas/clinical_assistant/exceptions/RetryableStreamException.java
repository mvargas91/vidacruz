package com.mvargas.clinical_assistant.exceptions;

public class RetryableStreamException extends RuntimeException {

    public RetryableStreamException(String message) {
        super(message);
    }
}
