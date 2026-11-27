package com.mvargas.clinical_assistant.exceptions;

import com.mvargas.enums.PatientStatus;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class PatientStatusConflictException extends ResponseStatusException {
    public PatientStatusConflictException(PatientStatus status, PatientStatus newStatus) {
        super(HttpStatus.CONFLICT, "Invalid transition: you cannot go from" + status + " to " + newStatus);
    }
}
