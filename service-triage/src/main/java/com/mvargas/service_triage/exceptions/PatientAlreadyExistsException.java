package com.mvargas.service_triage.exceptions;

public class PatientAlreadyExistsException extends RuntimeException {
    public PatientAlreadyExistsException(String name) {
        super("Ya existe un producto con el nombre " + name);
    }
}
