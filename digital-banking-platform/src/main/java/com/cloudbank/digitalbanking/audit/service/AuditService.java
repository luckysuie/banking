package com.cloudbank.digitalbanking.audit.service;

import com.cloudbank.digitalbanking.audit.enums.AuditAction;
import com.cloudbank.digitalbanking.audit.enums.AuditResult;
import com.cloudbank.digitalbanking.common.dto.PageResponse;
import com.cloudbank.digitalbanking.audit.dto.AuditResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AuditService {

    void recordEvent(
            String actorId,
            AuditAction action,
            String resourceType,
            String resourceId,
            String description,
            AuditResult result);

    void recordEventInNewTransaction(
            String actorId,
            AuditAction action,
            String resourceType,
            String resourceId,
            String description,
            AuditResult result);

    PageResponse<AuditResponse> searchEvents(
            AuditAction action,
            String resourceType,
            String actorId,
            AuditResult result,
            Pageable pageable);

    PageResponse<AuditResponse> getEventsByResource(String resourceType, String resourceId, Pageable pageable);

    List<AuditResponse> getEventsByCorrelationId(String correlationId);
}
