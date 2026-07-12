package com.cloudbank.digitalbanking.beneficiary.exception;

public class BeneficiaryNotFoundException extends RuntimeException {

    public BeneficiaryNotFoundException(String message) {
        super(message);
    }
}
