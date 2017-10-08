
package com.epam.lstrsum;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;

@Aspect
@Component
@Slf4j
public class LoggingAspect {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Around("execution(* com.epam.lstrsum.controller..*(..)) && ! execution(* com.epam.lstrsum.controller.QuestionController.addQuestion(..))")
    public Object logMethodExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        return logMethod(joinPoint, 0);
    }

    @Around("execution(* com.epam.lstrsum.controller.QuestionController.addQuestion(..))")
    public Object logMethodExecutionMultipartFile(ProceedingJoinPoint joinPoint) throws Throwable {
        return logMethod(joinPoint, 1);
    }

    private Object logMethod(ProceedingJoinPoint joinPoint, int paramsType) throws Throwable { // paramsType: 0 - no MultipartFile parameter, 1 - MultipartFile parameter present
        Object[] args = joinPoint.getArgs();
        String methodName = joinPoint.getSignature().getName();


        if(paramsType == 0){
            log.debug("{} method called with args {}", methodName, OBJECT_MAPPER.writeValueAsString(args));
        }
        if(paramsType == 1){
            log.debug("{} method called with args {}", methodName, OBJECT_MAPPER.writeValueAsString(modifyArgsForLogging(args)));
        }

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
            log.error("{} method thrown exception {}", methodName, t.getMessage());
            throw t;
        }

        return result;
    }

    private Object[] modifyArgsForLogging(Object[] args) {
        val argsModified = new ArrayList<Object>();

        // replace attachments with their filenames
        for(Object arg: args){
            if(arg instanceof MultipartFile[]){
                StringBuilder fileNames = new StringBuilder("MultipartFile[]: ");
                for(MultipartFile file: (MultipartFile[]) arg){
                    fileNames.append(file.getOriginalFilename() + ", ");
                }
                argsModified.add(fileNames.substring(0, fileNames.length()-2));
            }
            else if(arg instanceof MultipartFile){
                argsModified.add(((MultipartFile) arg).getOriginalFilename());
            }
            else{
                argsModified.add(arg);
            }
        }
        return argsModified.toArray();
    }
}
