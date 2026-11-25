package com.mvargas.service_triage.exceptions;

public class PatientNotFoundException extends RuntimeException {
    public PatientNotFoundException(Long id) {
        super("Producto con id " + id + " no encontrado");
    }
}
