package com.cloudbank.digitalbanking.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ValidationErrorResponse {

    private Instant timestamp;
    private int status;
    private ErrorCode errorCode;
    private String message;
    private String path;
    private String correlationId;
    private List<FieldValidationError> fieldErrors;

    @Getter
    @Builder
    public static class FieldValidationError {
        private String field;
        private String message;
        private Object rejectedValue;
    }
}
