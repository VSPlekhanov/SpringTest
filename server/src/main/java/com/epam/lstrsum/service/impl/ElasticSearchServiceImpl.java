package com.epam.lstrsum.service.impl;

import com.epam.lstrsum.service.ElasticSearchService;
import com.google.common.collect.ImmutableMap;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import java.io.IOException;

import static java.lang.String.valueOf;

@Service
@Slf4j
@ConfigurationProperties(prefix = "elastic")
public class ElasticSearchServiceImpl implements ElasticSearchService {
    @Setter
    private String index;

    @Autowired
    private RestClient restClient;

    public String smartSearch(String searchQuery, int page, int size) {
        final ImmutableMap<String, String> params = new ImmutableMap.Builder<String, String>()
                .put("pretty", "true")
                .put("size", valueOf(size))
                .put("from", valueOf(page * size))
                .build();

        try {
            return EntityUtils.toString(
                    restClient.performRequest(
                            "GET", String.format(ENDPOINT, index), params,
                            new NStringEntity(String.format(QUESTION_SEARCH, searchQuery), ContentType.APPLICATION_JSON)
                    ).getEntity());
        } catch (IOException e) {
            log.warn("Can't perform request to ES, with error {}", e.getMessage());
        }
        return "";
    }
}
