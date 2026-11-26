package com.mvargas.service_triage.rules;

import com.mvargas.service_triage.enums.SlaLevel;
import com.mvargas.service_triage.models.request.PatientRq;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.Optional;
import java.util.Set;

@Component
@Order(4)
public class NoUrgentTwoRule implements SlaRule {

    private static final Set<String> ADMINISTRATIVE_COMPLAINTS = Set.of(
            "renovacion de formula medica",
            "renovacion de recetario",
            "solicitud de certificado medico",
            "solicitud de incapacidad",
            "reclamo de resultados de laboratorio",
            "autorizacion de procedimiento",
            "solicitud de historia clinica",
            "cita de control",
            "tramite de remision"
    );

    @Override
    public Optional<SlaLevel> evaluate(final PatientRq request) {
        final String complaint = normalize(request.getDescription());
        return ADMINISTRATIVE_COMPLAINTS.contains(complaint)
                ? Optional.of(SlaLevel.NO_URGENTE_2)
                : Optional.empty();
    }

    private String normalize(final String text) {
        return Normalizer.normalize(text.trim().toLowerCase(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
    }
}
