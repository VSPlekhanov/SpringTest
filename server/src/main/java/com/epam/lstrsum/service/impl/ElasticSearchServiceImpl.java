package com.epam.lstrsum.service.impl;

import com.epam.lstrsum.model.Question;
import com.epam.lstrsum.service.ElasticSearchService;
import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

import static java.lang.String.valueOf;

@Service
@Slf4j
@ConfigurationProperties(prefix = "elastic")
@RequiredArgsConstructor
public class ElasticSearchServiceImpl implements ElasticSearchService {
    @Setter
    private String index;

    @Autowired
    private RestClient restClient;

    @Autowired
    RestHighLevelClient restHighLevelClient;

    @Setter
    private List<String> validMetaTags;

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

    // TODO: 10/20/2017 Create a smart search only in questions on which the user is allowed sub
    @Override
    public String smartSearchWithAllowedSub(String searchQuery, int page, int size, String email) {
        throw new UnsupportedOperationException("Unsupported method yet");
    }

    @Override
    public String advancedSearch(String searchString, List<String> metaTags, Integer page, Integer size) {
        try {
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                    .query(createQuery(searchString, metaTags))
                    .from(page)
                    .size(size)
                    .sort("createdAt", SortOrder.DESC)
                    .highlighter(createHighlighter());
            SearchRequest searchRequest = new SearchRequest(index)
                    .types(Question.QUESTION_COLLECTION_NAME)
                    .source(searchSourceBuilder);
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest);
            return searchResponse.toString();
        } catch (IOException e){
            log.warn("Can't perform request to ES, with error {}", e.getMessage());
        }
        return "";

    }

    private BoolQueryBuilder createQuery(String searchString, List<String> metaTags){
        QueryBuilder queryBuilder;

        if (!searchString.isEmpty()) {
            queryBuilder = QueryBuilders
                    .multiMatchQuery(searchString)
                    .field("title", 2)
                    .field("text")
                    .field("tags", 2)
                    .fuzziness(Fuzziness.AUTO)
                    .type(MultiMatchQueryBuilder.Type.MOST_FIELDS)
                    .operator(Operator.OR)
                    .analyzer("custom-analyzer");
        } else {
            queryBuilder = QueryBuilders.matchAllQuery();
        }

        BoolQueryBuilder query = QueryBuilders.boolQuery().must(queryBuilder);
        if (!metaTags.isEmpty()) {
            query.filter(createFilter(metaTags));
        }
        return query;
    }

    private BoolQueryBuilder createFilter(List<String> metaTags){
        BoolQueryBuilder metatagBoolFilter = QueryBuilders.boolQuery();
        metaTags.forEach(
                (el) -> metatagBoolFilter.should(
                        QueryBuilders.queryStringQuery(el)
                .defaultOperator(Operator.AND))
                //.analyzer("custom-analyzer"))
        );
        return metatagBoolFilter;
    }

    private HighlightBuilder createHighlighter(){
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        validMetaTags.forEach(
                el -> highlightBuilder.field(new HighlightBuilder.Field(el))
        );
        return highlightBuilder;
    }

}
