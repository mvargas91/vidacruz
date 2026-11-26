package com.mvargas.service_triage.models.entities;

import com.mvargas.service_triage.enums.OutboxStatus;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Table("outbox_event")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class OutboxEvent {
    @Id
    private Long id;
    private UUID eventId;
    private String aggregateType;
    private String aggregateId;
    private String eventType;
    private String payload;
    private OutboxStatus status;
    private Integer attempts;
    private String lastError;
    private LocalDateTime createdAt;
    private LocalDateTime publishedAt;
}
