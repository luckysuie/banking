package com.cloudbank.digitalbanking.audit.context;

import com.cloudbank.digitalbanking.common.util.ReferenceGenerator;

/**
 * Thread-local holder for the current request correlation ID.
 */
public final class AuditCorrelationContext {

    private static final ThreadLocal<String> CORRELATION_ID = new ThreadLocal<>();

    private AuditCorrelationContext() {
    }

    public static void set(String correlationId) {
        CORRELATION_ID.set(correlationId);
    }

    public static String get() {
        return CORRELATION_ID.get();
    }

    public static String getOrGenerate() {
        String correlationId = CORRELATION_ID.get();
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = ReferenceGenerator.generateUuid().toString();
            CORRELATION_ID.set(correlationId);
        }
        return correlationId;
    }

    public static void clear() {
        CORRELATION_ID.remove();
    }
}
