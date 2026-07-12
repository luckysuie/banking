package com.cloudbank.digitalbanking.exception;

import com.cloudbank.digitalbanking.exception.ErrorCode;
import com.cloudbank.digitalbanking.exception.ValidationErrorResponse;
import com.cloudbank.digitalbanking.account.exception.AccountNotFoundException;
import com.cloudbank.digitalbanking.payment.exception.InsufficientBalanceException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    void setUp() {
        when(request.getRequestURI()).thenReturn("/api/customers/123");
    }

    @AfterEach
    void tearDown() {
        com.cloudbank.digitalbanking.audit.context.AuditCorrelationContext.clear();
    }

    @Test
    void handleResourceNotFound_shouldReturnStructuredErrorResponse() {
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleResourceNotFound(
                new AccountNotFoundException("Account not found"),
                request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);
        assertThat(response.getBody().getPath()).isEqualTo("/api/customers/123");
        assertThat(response.getBody().getCorrelationId()).isNotBlank();
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }

    @Test
    void handleInsufficientBalance_shouldReturnCorrectErrorCode() {
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleInsufficientBalance(
                new InsufficientBalanceException("Insufficient available balance"),
                request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getErrorCode()).isEqualTo(ErrorCode.INSUFFICIENT_BALANCE);
    }

    @Test
    void handleValidationErrors_shouldReturnFieldErrors_whenRequestIsInvalid() {
        BindException bindException = new BindException(new Object(), "customerRequest");
        bindException.addError(new FieldError("customerRequest", "email", "invalid", false,
                new String[]{"Email"}, null, "Email must be valid"));

        ResponseEntity<ValidationErrorResponse> response =
                globalExceptionHandler.handleValidationErrors(bindException, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo(ErrorCode.VALIDATION_ERROR);
        assertThat(response.getBody().getFieldErrors()).hasSize(1);
        assertThat(response.getBody().getFieldErrors().getFirst().getField()).isEqualTo("email");
        assertThat(response.getBody().getFieldErrors().getFirst().getMessage()).isEqualTo("Email must be valid");
    }

    @Test
    void handleOptimisticLockingFailure_shouldReturnConflict() {
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleOptimisticLockingFailure(
                new ObjectOptimisticLockingFailureException(Object.class, "id"),
                request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().getErrorCode()).isEqualTo(ErrorCode.CONCURRENT_MODIFICATION);
    }

    @Test
    void handleIllegalArgument_shouldReturnBadRequest() {
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleIllegalArgument(
                new IllegalArgumentException("Current balance cannot be negative"),
                request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getErrorCode()).isEqualTo(ErrorCode.VALIDATION_ERROR);
    }

    @Test
    void handleUnexpectedException_shouldNotExposeInternalDetails() {
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleUnexpectedException(
                new RuntimeException("Sensitive internal failure"),
                request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getErrorCode()).isEqualTo(ErrorCode.INTERNAL_ERROR);
        assertThat(response.getBody().getMessage()).doesNotContain("Sensitive");
    }
}
