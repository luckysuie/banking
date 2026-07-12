package com.cloudbank.digitalbanking.audit.mapper;

import com.cloudbank.digitalbanking.audit.context.AuditCorrelationContext;
import com.cloudbank.digitalbanking.audit.dto.AuditResponse;
import com.cloudbank.digitalbanking.audit.entity.AuditEvent;
import com.cloudbank.digitalbanking.audit.enums.AuditAction;
import com.cloudbank.digitalbanking.audit.enums.AuditResult;
import com.cloudbank.digitalbanking.common.util.ReferenceGenerator;

public final class AuditEventMapper {

    private AuditEventMapper() {
    }

    public static AuditEvent toEntity(
            String actorId,
            AuditAction action,
            String resourceType,
            String resourceId,
            String description,
            AuditResult result) {
        AuditEvent event = new AuditEvent();
        event.setEventReference(ReferenceGenerator.generateAuditEventReference());
        event.setActorId(actorId);
        event.setAction(action);
        event.setResourceType(resourceType);
        event.setResourceId(resourceId);
        event.setDescription(description);
        event.setResult(result);
        event.setCorrelationId(AuditCorrelationContext.getOrGenerate());
        return event;
    }

    public static AuditResponse toResponse(AuditEvent event) {
        return AuditResponse.builder()
                .id(event.getId())
                .eventReference(event.getEventReference())
                .actorId(event.getActorId())
                .action(event.getAction())
                .resourceType(event.getResourceType())
                .resourceId(event.getResourceId())
                .description(event.getDescription())
                .result(event.getResult())
                .correlationId(event.getCorrelationId())
                .createdAt(event.getCreatedAt())
                .build();
    }
}
