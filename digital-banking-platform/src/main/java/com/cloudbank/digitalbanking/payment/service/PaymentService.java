package com.cloudbank.digitalbanking.payment.service;

import com.cloudbank.digitalbanking.payment.dto.FundTransferRequest;
import com.cloudbank.digitalbanking.payment.dto.PaymentResponse;

import java.util.List;
import java.util.UUID;

public interface PaymentService {

    PaymentResponse processFundTransfer(FundTransferRequest request);

    PaymentResponse getPaymentById(UUID id);

    PaymentResponse getPaymentByReference(String paymentReference);

    List<PaymentResponse> getPaymentsByCustomerId(UUID customerId);
}
