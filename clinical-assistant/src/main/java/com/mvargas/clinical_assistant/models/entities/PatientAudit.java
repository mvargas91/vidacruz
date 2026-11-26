package com.mvargas.clinical_assistant.models.entities;

import com.mvargas.clinical_assistant.enums.EventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;

@Data
@Table(name = "patient_audit")
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class PatientAudit {

    @Id
    private Long id;

    @Column(value = "patient_id")
    private Long patientId;

    @Column(value = "previous_state")
    private String previousState;

    @Column(value = "new_status")
    private String newStatus;

    @Column(value = "event_type")
    private EventType eventType;

    @Column(value = "event_date")
    private OffsetDateTime eventDate;
}
