package com.cloudbank.digitalbanking.audit.dto;

import com.cloudbank.digitalbanking.audit.enums.AuditAction;
import com.cloudbank.digitalbanking.audit.enums.AuditResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@Schema(description = "Audit event response payload")
public class AuditResponse {

    private UUID id;
    private String eventReference;
    private String actorId;
    private AuditAction action;
    private String resourceType;
    private String resourceId;
    private String description;
    private AuditResult result;
    private String correlationId;
    private LocalDateTime createdAt;
}
