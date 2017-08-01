package com.epam.lstrsum.mail.exception;

public class NoMailTemplateFoundException extends Exception {
    public NoMailTemplateFoundException() {
    }

    public NoMailTemplateFoundException(String message) {
        super(message);
    }

    public NoMailTemplateFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoMailTemplateFoundException(Throwable cause) {
        super(cause);
    }

    public NoMailTemplateFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
