package com.cloudbank.digitalbanking.payment.mapper;

import com.cloudbank.digitalbanking.common.util.ReferenceGenerator;
import com.cloudbank.digitalbanking.payment.dto.FundTransferRequest;
import com.cloudbank.digitalbanking.payment.dto.PaymentResponse;
import com.cloudbank.digitalbanking.payment.entity.Payment;
import com.cloudbank.digitalbanking.payment.enums.PaymentStatus;

public final class PaymentMapper {

    private PaymentMapper() {
    }

    public static Payment toEntity(
            FundTransferRequest request,
            String destinationAccountNumber,
            String currency) {
        Payment payment = new Payment();
        payment.setPaymentReference(ReferenceGenerator.generatePaymentReference());
        payment.setSourceAccountId(request.getSourceAccountId());
        payment.setBeneficiaryId(request.getBeneficiaryId());
        payment.setDestinationAccountNumber(destinationAccountNumber);
        payment.setAmount(com.cloudbank.digitalbanking.common.util.MoneyUtils.normalize(request.getAmount()));
        payment.setCurrency(currency);
        payment.setDescription(request.getDescription());
        payment.setIdempotencyKey(request.getIdempotencyKey());
        payment.setStatus(PaymentStatus.RECEIVED);
        return payment;
    }

    public static PaymentResponse toResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .paymentReference(payment.getPaymentReference())
                .sourceAccountId(payment.getSourceAccountId())
                .beneficiaryId(payment.getBeneficiaryId())
                .destinationAccountNumber(payment.getDestinationAccountNumber())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .description(payment.getDescription())
                .idempotencyKey(payment.getIdempotencyKey())
                .status(payment.getStatus())
                .failureReason(payment.getFailureReason())
                .createdAt(payment.getCreatedAt())
                .completedAt(payment.getCompletedAt())
                .build();
    }
}
