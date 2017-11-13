package com.epam.lstrsum.configuration;

import com.epam.lstrsum.converter.QuestionPostDtoHttpMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class HttpConfiguration {
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public QuestionPostDtoHttpMessageConverter commonsMultipartResolver() {
        return new QuestionPostDtoHttpMessageConverter();
    }

}