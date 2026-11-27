package com.mvargas.clinical_assistant.models.response;

import com.mvargas.clinical_assistant.enums.NivelTriaje;
import com.mvargas.clinical_assistant.enums.PatientStatus;
import com.mvargas.clinical_assistant.enums.SlaLevel;
import com.mvargas.clinical_assistant.enums.TipoEvento;

public record TriageEvent(TipoEvento status, Long patientId, NivelTriaje slaLevel) {
}
