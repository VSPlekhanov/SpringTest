package com.epam.lstrsum.email;

import com.epam.lstrsum.email.exception.NoMailTemplateFoundException;
import com.epam.lstrsum.email.service.MailService;
import com.epam.lstrsum.email.template.MailTemplate;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.List;

@Aspect
@Component
@Profile("email")
@Slf4j
public class EmailNotificationAspect {

    private final List<MailTemplate> templates;
    private final MailService mailService;

    @Autowired
    public EmailNotificationAspect(List<MailTemplate> templates, MailService mailService) {
        this.templates = templates;
        this.mailService = mailService;
    }

    @SuppressWarnings("unchecked")
    @Around("@annotation(EmailNotification) && execution(* *(..))")
    public Object aroundAdvice(ProceedingJoinPoint joinPoint) throws Throwable {
        Object proceed = joinPoint.proceed();
        try {
            EmailNotification annotation = getAnnotation(joinPoint, EmailNotification.class);
            Class<? extends MailTemplate> templateClass = annotation.template();

            MailTemplate template = templates.stream()
                    .filter(templateClass::isInstance)
                    .findFirst()
                    .orElseThrow(() -> new NoMailTemplateFoundException("Can not found template bean of type: " + templateClass));

            mailService.sendMessage(template.buildMailMessage(proceed, annotation.fromPortal()));
        } catch (Exception e) {
            log.error("Error sending email notification. {}", e.getMessage());
        }

        return proceed;
    }

    private EmailNotification getAnnotation(ProceedingJoinPoint joinPoint, Class<EmailNotification> annotationClass) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        return method.getAnnotation(annotationClass);
    }
}
