package com.epam.lstrsum.configuration;

import com.epam.lstrsum.service.AnswerService;
import com.epam.lstrsum.service.QuestionService;
import com.epam.lstrsum.service.UserService;
import com.epam.lstrsum.service.mail.UserSynchronizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import static org.mockito.Mockito.mock;

@Profile("controller-security-test")
@Configuration
public class ControllerSecurityTestConfiguration {

    @Bean
    @Primary
    public QuestionService questionService() {
        return mock(QuestionService.class);
    }

    @Bean
    @Primary
    public AnswerService answerService() {
        return mock(AnswerService.class);
    }

    @Bean
    @Primary
    public UserService userService() {
        return mock(UserService.class);
    }

    @Bean
    @Primary
    public UserSynchronizer userSynchronizer() {
        return mock(UserSynchronizer.class);
    }
}