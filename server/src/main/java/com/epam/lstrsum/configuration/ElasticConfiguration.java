package com.epam.lstrsum.configuration;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "elastic")
@Slf4j
public class ElasticConfiguration {
    @Setter
    private String host;

    @Setter
    private int port;

    @Bean
    public RestClient restClient() {
        log.debug("Building elastic on: " + host + ":" + port);
        return RestClient.builder(new HttpHost(host, port, "http")).build();
    }

    @Bean
    public RestHighLevelClient restHighLevelClient() {
        return new RestHighLevelClient(restClient());
    }
}
