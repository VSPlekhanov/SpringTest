package com.epam.lstrsum.exception;

public class NoSuchRequestException extends RuntimeException {
    public NoSuchRequestException(String message) {
        super(message);
    }
}
