package com.mvargas.clinical_assistant.exceptions;

public class PatientAlreadyExistsException extends RuntimeException {
    public PatientAlreadyExistsException(String name) {
        super("A patient with that name "+ name +" already exists.");
    }
}
