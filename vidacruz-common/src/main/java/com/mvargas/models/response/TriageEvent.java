package com.mvargas.models.response;

import com.mvargas.enums.NivelTriaje;
import com.mvargas.enums.TipoEvento;

public record TriageEvent(TipoEvento status, Long patientId, NivelTriaje slaLevel) {
}
