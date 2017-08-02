package com.epam.lstrsum.mail.exception;

public class EmailValidationException extends RuntimeException {
    public EmailValidationException(String message){
        super(message);
    }
}
