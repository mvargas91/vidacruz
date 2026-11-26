package com.mvargas.clinical_assistant.exceptions;

public class PatientNotFoundException extends RuntimeException {
    public PatientNotFoundException(Long id) {
        super("Patient with ID " + id + " was not found.");
    }
}
