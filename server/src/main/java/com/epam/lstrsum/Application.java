package com.epam.lstrsum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.support.ClassPathXmlApplicationContext;

@SpringBootApplication
public class Application {

    public static void main(String[] args) throws Exception {
        new ClassPathXmlApplicationContext("application-context.xml");

        SpringApplication.run(new Object[]{Application.class}, args);
    }
}