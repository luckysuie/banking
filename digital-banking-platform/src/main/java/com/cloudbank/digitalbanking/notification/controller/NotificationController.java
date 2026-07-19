package com.cloudbank.digitalbanking.notification.controller;

import com.cloudbank.digitalbanking.common.dto.ApiResponse;
import com.cloudbank.digitalbanking.notification.dto.NotificationResponse;
import com.cloudbank.digitalbanking.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "In-app notification APIs")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get all notifications for a customer")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "Notifications retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "Customer not found")
    })
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getNotificationsByCustomer(
            @Parameter(description = "Customer UUID") @PathVariable UUID customerId) {
        return ResponseEntity.ok(ApiResponse.success(
                notificationService.getNotificationsByCustomerId(customerId)));
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Mark a notification as read")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "Notification marked as read"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "Notification not found")
    })
    public ResponseEntity<ApiResponse<NotificationResponse>> markNotificationAsRead(
            @Parameter(description = "Notification UUID") @PathVariable UUID id) {
        NotificationResponse response = notificationService.markAsRead(id);
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read", response));
    }
}
