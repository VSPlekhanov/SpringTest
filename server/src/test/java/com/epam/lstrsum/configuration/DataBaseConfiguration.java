package com.epam.lstrsum.configuration;

import com.github.fakemongo.Fongo;
import com.mongodb.MongoClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataBaseConfiguration {

    @Bean
    public MongoClient mongoClient() {
        return new Fongo("ExperienceTestDataBase").getMongo();
    }

}

