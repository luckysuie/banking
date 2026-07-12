package com.cloudbank.digitalbanking.audit.repository;

import com.cloudbank.digitalbanking.audit.entity.AuditEvent;
import com.cloudbank.digitalbanking.audit.enums.AuditAction;
import com.cloudbank.digitalbanking.audit.enums.AuditResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface AuditRepository extends JpaRepository<AuditEvent, UUID> {

    Page<AuditEvent> findByResourceTypeAndResourceIdOrderByCreatedAtDesc(
            String resourceType,
            String resourceId,
            Pageable pageable);

    Page<AuditEvent> findByCorrelationIdOrderByCreatedAtDesc(String correlationId, Pageable pageable);

    @Query("""
            SELECT e FROM AuditEvent e
            WHERE (:action IS NULL OR e.action = :action)
              AND (:resourceType IS NULL OR e.resourceType = :resourceType)
              AND (:actorId IS NULL OR e.actorId = :actorId)
              AND (:result IS NULL OR e.result = :result)
            ORDER BY e.createdAt DESC
            """)
    Page<AuditEvent> findWithFilters(
            @Param("action") AuditAction action,
            @Param("resourceType") String resourceType,
            @Param("actorId") String actorId,
            @Param("result") AuditResult result,
            Pageable pageable);

    List<AuditEvent> findByCorrelationIdOrderByCreatedAtDesc(String correlationId);
}
