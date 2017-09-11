package com.epam.lstrsum.testutils;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

public class AssertionUtils {
    public static <T> void hasStatusOk(ResponseEntity<T> responseEntity) {
        assertEntityWithStatus(responseEntity, HttpStatus.OK);
    }

    public static <T> void hasStatusNotFound(ResponseEntity<T> responseEntity) {
        assertEntityWithStatus(responseEntity, HttpStatus.NOT_FOUND);
    }

    public static <T> void hasStatusNoContent(ResponseEntity<T> responseEntity) {
        assertEntityWithStatus(responseEntity, HttpStatus.NO_CONTENT);
    }

    private static <T> void assertEntityWithStatus(ResponseEntity<T> responseEntity, HttpStatus httpStatus) {
        assertThat(responseEntity.getStatusCode())
                .isEqualTo(httpStatus);
    }
}
