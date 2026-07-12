package com.cloudbank.digitalbanking.exception;

import com.cloudbank.digitalbanking.account.exception.AccountNotFoundException;
import com.cloudbank.digitalbanking.account.exception.InvalidAccountStatusException;
import com.cloudbank.digitalbanking.audit.context.AuditCorrelationContext;
import com.cloudbank.digitalbanking.beneficiary.exception.BeneficiaryNotFoundException;
import com.cloudbank.digitalbanking.customer.exception.CustomerNotFoundException;
import com.cloudbank.digitalbanking.notification.exception.NotificationNotFoundException;
import com.cloudbank.digitalbanking.payment.exception.DuplicatePaymentException;
import com.cloudbank.digitalbanking.payment.exception.InsufficientBalanceException;
import com.cloudbank.digitalbanking.payment.exception.InvalidTransferException;
import com.cloudbank.digitalbanking.payment.exception.PaymentNotFoundException;
import com.cloudbank.digitalbanking.transaction.exception.TransactionNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler({
            ResourceNotFoundException.class,
            CustomerNotFoundException.class,
            AccountNotFoundException.class,
            BeneficiaryNotFoundException.class,
            PaymentNotFoundException.class,
            TransactionNotFoundException.class,
            NotificationNotFoundException.class
    })
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            RuntimeException ex,
            HttpServletRequest request) {
        return buildErrorResponse(
                HttpStatus.NOT_FOUND,
                ErrorCode.RESOURCE_NOT_FOUND,
                ex.getMessage(),
                request);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResource(
            DuplicateResourceException ex,
            HttpServletRequest request) {
        return buildErrorResponse(
                HttpStatus.CONFLICT,
                ErrorCode.DUPLICATE_RESOURCE,
                ex.getMessage(),
                request);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<ValidationErrorResponse> handleValidationErrors(
            Exception ex,
            HttpServletRequest request) {
        List<ValidationErrorResponse.FieldValidationError> fieldErrors = extractFieldErrors(ex);
        ValidationErrorResponse body = ValidationErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .errorCode(ErrorCode.VALIDATION_ERROR)
                .message("Validation failed")
                .path(request.getRequestURI())
                .correlationId(resolveCorrelationId())
                .fieldErrors(fieldErrors)
                .build();
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientBalance(
            InsufficientBalanceException ex,
            HttpServletRequest request) {
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                ErrorCode.INSUFFICIENT_BALANCE,
                ex.getMessage(),
                request);
    }

    @ExceptionHandler(InvalidTransferException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTransfer(
            InvalidTransferException ex,
            HttpServletRequest request) {
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                ErrorCode.INVALID_TRANSFER,
                ex.getMessage(),
                request);
    }

    @ExceptionHandler(DuplicatePaymentException.class)
    public ResponseEntity<ErrorResponse> handleDuplicatePayment(
            DuplicatePaymentException ex,
            HttpServletRequest request) {
        return buildErrorResponse(
                HttpStatus.CONFLICT,
                ErrorCode.DUPLICATE_PAYMENT,
                ex.getMessage(),
                request);
    }

    @ExceptionHandler(InvalidAccountStatusException.class)
    public ResponseEntity<ErrorResponse> handleInvalidAccountStatus(
            InvalidAccountStatusException ex,
            HttpServletRequest request) {
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                ErrorCode.INVALID_ACCOUNT_STATUS,
                ex.getMessage(),
                request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex,
            HttpServletRequest request) {
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                ErrorCode.VALIDATION_ERROR,
                ex.getMessage(),
                request);
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLockingFailure(
            ObjectOptimisticLockingFailureException ex,
            HttpServletRequest request) {
        return buildErrorResponse(
                HttpStatus.CONFLICT,
                ErrorCode.CONCURRENT_MODIFICATION,
                "The resource was modified by another request. Please retry.",
                request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {
        String message = "Invalid value for parameter '" + ex.getName() + "'";
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR, message, request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                ErrorCode.VALIDATION_ERROR,
                "Malformed or unreadable request body",
                request);
    }

    @ExceptionHandler({DataIntegrityViolationException.class, DataAccessException.class})
    public ResponseEntity<ErrorResponse> handleDatabaseErrors(
            Exception ex,
            HttpServletRequest request) {
        log.error("Database error on path {}: {}", request.getRequestURI(), ex.getMessage());
        return buildErrorResponse(
                HttpStatus.CONFLICT,
                ErrorCode.DATABASE_ERROR,
                "A data persistence error occurred. Please verify your request and try again.",
                request);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException ex,
            HttpServletRequest request) {
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                ErrorCode.VALIDATION_ERROR,
                ex.getMessage(),
                request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(
            Exception ex,
            HttpServletRequest request) {
        log.error("Unexpected error on path {}", request.getRequestURI(), ex);
        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCode.INTERNAL_ERROR,
                "An unexpected error occurred. Please try again later.",
                request);
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(
            HttpStatus status,
            ErrorCode errorCode,
            String message,
            HttpServletRequest request) {
        ErrorResponse body = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .errorCode(errorCode)
                .message(message)
                .path(request.getRequestURI())
                .correlationId(resolveCorrelationId())
                .build();
        return ResponseEntity.status(status).body(body);
    }

    private String resolveCorrelationId() {
        return AuditCorrelationContext.getOrGenerate();
    }

    private List<ValidationErrorResponse.FieldValidationError> extractFieldErrors(Exception ex) {
        if (ex instanceof MethodArgumentNotValidException manve) {
            return manve.getBindingResult().getFieldErrors().stream()
                    .map(this::toFieldValidationError)
                    .toList();
        }
        if (ex instanceof BindException be) {
            return be.getBindingResult().getFieldErrors().stream()
                    .map(this::toFieldValidationError)
                    .toList();
        }
        return List.of();
    }

    private ValidationErrorResponse.FieldValidationError toFieldValidationError(
            org.springframework.validation.FieldError error) {
        return ValidationErrorResponse.FieldValidationError.builder()
                .field(error.getField())
                .message(error.getDefaultMessage())
                .rejectedValue(error.getRejectedValue())
                .build();
    }
}
