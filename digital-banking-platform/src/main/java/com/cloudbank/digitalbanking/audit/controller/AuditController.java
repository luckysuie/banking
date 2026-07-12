package com.cloudbank.digitalbanking.audit.controller;

import com.cloudbank.digitalbanking.audit.dto.AuditResponse;
import com.cloudbank.digitalbanking.audit.enums.AuditAction;
import com.cloudbank.digitalbanking.audit.enums.AuditResult;
import com.cloudbank.digitalbanking.audit.service.AuditService;
import com.cloudbank.digitalbanking.common.dto.ApiResponse;
import com.cloudbank.digitalbanking.common.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/audit-events")
@RequiredArgsConstructor
@Tag(name = "Audit", description = "Read-only audit trail APIs")
public class AuditController {

    private final AuditService auditService;

    @GetMapping
    @Operation(
            summary = "Search audit events",
            description = "Supports optional filtering by action, resource type, actor ID and result with pagination"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "Audit events retrieved successfully")
    })
    public ResponseEntity<ApiResponse<PageResponse<AuditResponse>>> searchAuditEvents(
            @Parameter(description = "Optional audit action filter")
            @RequestParam(required = false) AuditAction action,
            @Parameter(description = "Optional resource type filter (e.g. CUSTOMER, ACCOUNT)")
            @RequestParam(required = false) String resourceType,
            @Parameter(description = "Optional actor ID filter")
            @RequestParam(required = false) String actorId,
            @Parameter(description = "Optional result filter (SUCCESS or FAILURE)")
            @RequestParam(required = false) AuditResult result,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                auditService.searchEvents(action, resourceType, actorId, result, pageable)));
    }

    @GetMapping("/resource/{resourceType}/{resourceId}")
    @Operation(summary = "Get audit events for a specific resource")
    public ResponseEntity<ApiResponse<PageResponse<AuditResponse>>> getAuditEventsByResource(
            @Parameter(description = "Resource type (e.g. CUSTOMER, ACCOUNT, PAYMENT)")
            @PathVariable String resourceType,
            @Parameter(description = "Resource identifier")
            @PathVariable String resourceId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                auditService.getEventsByResource(resourceType, resourceId, pageable)));
    }

    @GetMapping("/correlation/{correlationId}")
    @Operation(summary = "Get audit events by correlation ID")
    public ResponseEntity<ApiResponse<List<AuditResponse>>> getAuditEventsByCorrelation(
            @Parameter(description = "Correlation ID from X-Correlation-Id header")
            @PathVariable String correlationId) {
        return ResponseEntity.ok(ApiResponse.success(
                auditService.getEventsByCorrelationId(correlationId)));
    }
}
