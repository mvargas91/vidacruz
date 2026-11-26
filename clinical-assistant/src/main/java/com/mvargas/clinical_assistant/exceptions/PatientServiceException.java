package com.mvargas.clinical_assistant.exceptions;

public class PatientServiceException extends RuntimeException {
    public PatientServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
