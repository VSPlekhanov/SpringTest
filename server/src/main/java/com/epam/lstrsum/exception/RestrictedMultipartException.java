package com.epam.lstrsum.exception;

import org.springframework.web.multipart.MultipartException;

public class RestrictedMultipartException extends MultipartException {
    public RestrictedMultipartException(String msg) {
        super(msg);
    }

    public RestrictedMultipartException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
