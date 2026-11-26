package com.mvargas.clinical_assistant.exceptions;

public class PatientAlreadyExistsException extends RuntimeException {
    public PatientAlreadyExistsException(String name) {
        super("Ya existe un producto con el nombre " + name);
    }
}
