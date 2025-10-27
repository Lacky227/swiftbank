package com.swiftbank.accountservice.exceptions;

public class CreateAccountException extends RuntimeException {
    public CreateAccountException(String message) {
        super(message);
    }
}
