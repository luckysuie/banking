package com.cloudbank.digitalbanking.audit.service;

import com.cloudbank.digitalbanking.audit.dto.AuditResponse;
import com.cloudbank.digitalbanking.audit.entity.AuditEvent;
import com.cloudbank.digitalbanking.audit.enums.AuditAction;
import com.cloudbank.digitalbanking.audit.enums.AuditResult;
import com.cloudbank.digitalbanking.audit.mapper.AuditEventMapper;
import com.cloudbank.digitalbanking.audit.repository.AuditRepository;
import com.cloudbank.digitalbanking.common.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuditServiceImpl implements AuditService {

    private final AuditRepository auditRepository;

    @Override
    @Transactional
    public void recordEvent(
            String actorId,
            AuditAction action,
            String resourceType,
            String resourceId,
            String description,
            AuditResult result) {
        persistEvent(actorId, action, resourceType, resourceId, description, result);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordEventInNewTransaction(
            String actorId,
            AuditAction action,
            String resourceType,
            String resourceId,
            String description,
            AuditResult result) {
        persistEvent(actorId, action, resourceType, resourceId, description, result);
    }

    @Override
    public PageResponse<AuditResponse> searchEvents(
            AuditAction action,
            String resourceType,
            String actorId,
            AuditResult result,
            Pageable pageable) {
        Page<AuditEvent> page = auditRepository.findWithFilters(
                action, resourceType, actorId, result, pageable);
        return PageResponse.from(page, AuditEventMapper::toResponse);
    }

    @Override
    public PageResponse<AuditResponse> getEventsByResource(
            String resourceType,
            String resourceId,
            Pageable pageable) {
        Page<AuditEvent> page = auditRepository.findByResourceTypeAndResourceIdOrderByCreatedAtDesc(
                resourceType, resourceId, pageable);
        return PageResponse.from(page, AuditEventMapper::toResponse);
    }

    @Override
    public List<AuditResponse> getEventsByCorrelationId(String correlationId) {
        return auditRepository.findByCorrelationIdOrderByCreatedAtDesc(correlationId).stream()
                .map(AuditEventMapper::toResponse)
                .toList();
    }

    private void persistEvent(
            String actorId,
            AuditAction action,
            String resourceType,
            String resourceId,
            String description,
            AuditResult result) {
        auditRepository.save(AuditEventMapper.toEntity(
                actorId, action, resourceType, resourceId, sanitize(description), result));
    }

    private String sanitize(String description) {
        if (description == null) {
            return "";
        }
        return description;
    }
}
