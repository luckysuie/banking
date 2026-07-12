package com.cloudbank.digitalbanking.exception;

/**
 * Standardized API error codes returned to clients.
 */
public enum ErrorCode {

    RESOURCE_NOT_FOUND,
    DUPLICATE_RESOURCE,
    VALIDATION_ERROR,
    INSUFFICIENT_BALANCE,
    INVALID_TRANSFER,
    DUPLICATE_PAYMENT,
    INVALID_ACCOUNT_STATUS,
    DATABASE_ERROR,
    INTERNAL_ERROR,
    CONCURRENT_MODIFICATION,
    UNAUTHORIZED,
    FORBIDDEN
}
