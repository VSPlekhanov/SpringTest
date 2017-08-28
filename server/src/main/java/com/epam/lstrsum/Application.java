package com.epam.lstrsum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAspectJAutoProxy
public class Application {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(new Object[]{Application.class}, args);
    }
}