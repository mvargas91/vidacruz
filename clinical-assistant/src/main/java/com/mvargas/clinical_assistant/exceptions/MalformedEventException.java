package com.mvargas.clinical_assistant.exceptions;


public class MalformedEventException extends RuntimeException {
    public MalformedEventException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
