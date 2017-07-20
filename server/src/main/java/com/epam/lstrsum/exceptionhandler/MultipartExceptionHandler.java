package com.epam.lstrsum.exceptionhandler;

import com.epam.lstrsum.exception.RestrictedMultipartException;
import com.epam.lstrsum.exception.SizeLimitMultipartException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class MultipartExceptionHandler {

    @ExceptionHandler(SizeLimitMultipartException.class)
    public ResponseEntity<String> tooLargeFile(SizeLimitMultipartException e) {
        log.warn("Multipart exception --> " + e.getMessage());
        return ResponseEntity
                .status(HttpStatus.PAYLOAD_TOO_LARGE)
                .header("message", "File is to large")
                .body(e.getMessage());
    }

    @ExceptionHandler(RestrictedMultipartException.class)
    public ResponseEntity<String> restrictedFileType(RestrictedMultipartException e) {
        log.warn("Multipart exception --> " + e.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .header("message", "Restricted file format")
                .body(e.getMessage());
    }


}
