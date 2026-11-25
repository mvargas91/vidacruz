package com.mvargas.service_triage.exceptions;

public class PatientServiceException extends RuntimeException {
    public PatientServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
