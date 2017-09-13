package com.epam.lstrsum;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class LoggingAspect {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Around("execution(* com.epam.lstrsum.controller..*(..))")
    public Object logMethodExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        String methodName = joinPoint.getSignature().getName();

        log.debug("{} method called with args {}", methodName, OBJECT_MAPPER.writeValueAsString(args));

        Object result;
        try {
            result = joinPoint.proceed(args);

            if (result instanceof ResponseEntity) {
                val response = (ResponseEntity) result;
                log.debug("{} method status code = {}", methodName, response.getStatusCode());
            } else {
                log.debug("{} method was completed", methodName);
            }
        } catch (Throwable t) {
            log.error("{} method thorwn exception {}", methodName, t.getMessage());
            throw t;
        }

        return result;
    }

}
