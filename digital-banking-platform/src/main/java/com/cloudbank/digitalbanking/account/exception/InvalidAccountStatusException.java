package com.cloudbank.digitalbanking.account.exception;

public class InvalidAccountStatusException extends RuntimeException {

    public InvalidAccountStatusException(String message) {
        super(message);
    }
}
