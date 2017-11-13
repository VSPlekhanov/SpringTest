package com.epam.lstrsum.exception;

public class AnswersWithSameIdException extends RuntimeException {
    public AnswersWithSameIdException(String message) {
        super(message);
    }
}
