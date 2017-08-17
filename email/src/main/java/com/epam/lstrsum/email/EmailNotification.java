package com.epam.lstrsum.email;

import com.epam.lstrsum.email.template.MailTemplate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate methods to notify users thought email.
 * <p>
 * Use MailTemplate implementation to specify which template
 * should be used for notification.
 * <p>
 * MailTemplate generic parameter should be same as annotated method
 * return type.
 */

@Retention(value = RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EmailNotification {
    Class<? extends MailTemplate> template();
}
