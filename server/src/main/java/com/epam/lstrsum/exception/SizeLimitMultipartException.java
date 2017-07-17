package com.epam.lstrsum.exception;

import org.springframework.web.multipart.MultipartException;

public class SizeLimitMultipartException extends MultipartException {
    public SizeLimitMultipartException(String msg) {
        super(msg);
    }

    public SizeLimitMultipartException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
