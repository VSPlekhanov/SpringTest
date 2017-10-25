package com.epam.lstrsum.configuration;

import lombok.Setter;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "elastic")
public class ElasticConfiguration {
    @Setter
    private String host;

    @Setter
    private int port;

    @Bean
    public RestClient restClient() {
        return RestClient.builder(new HttpHost(host, port, "http")).build();
    }

    @Bean
    public RestHighLevelClient restHighLevelClient() {
        return new RestHighLevelClient(restClient());
    }
}
