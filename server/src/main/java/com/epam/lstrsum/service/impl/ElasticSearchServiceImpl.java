package com.epam.lstrsum.service.impl;

import com.epam.lstrsum.converter.UserDtoMapper;
import com.epam.lstrsum.dto.question.QuestionAllFieldsDto;
import com.epam.lstrsum.dto.question.QuestionAllFieldsListDto;
import com.epam.lstrsum.model.Question;
import com.epam.lstrsum.persistence.UserRepository;
import com.epam.lstrsum.service.ElasticSearchService;
import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
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
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.valueOf;

@Service
@Slf4j
@ConfigurationProperties(prefix = "elastic")
@RequiredArgsConstructor
public class ElasticSearchServiceImpl implements ElasticSearchService {

    // Pattern for getting id from string like "DBRef(u'User', ObjectId('5a1571563867e59abcba48ca'))"
    private final Pattern dbRefPattern = Pattern.compile("^.*\\'(\\w*)\\'\\)\\)$");
    private final String elsticDateTimePattern = "yyyy-MM-dd'T'HH:mm:ss.SSS";

    @Setter
    private String index;

    @Autowired
    private RestClient restClient;

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Setter
    private List<String> validMetaTags;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserDtoMapper userMapper;

    @Override
    public QuestionAllFieldsListDto elasticSimpleSearch(String searchString, List<String> metaTags, Integer page, Integer size) {
        List<QuestionAllFieldsDto> result = new ArrayList<>();

        try {
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                    .query(createQuery(searchString, metaTags))
                    .from(page)
                    .size(size)
                    .sort("createdAt", SortOrder.DESC)
                    .highlighter(createHighlighter());

            String[] includeFields = new String[]{"title", "createdAt", "authorId"};
            String[] excludeFields = new String[]{""};
            searchSourceBuilder.fetchSource(includeFields, excludeFields);

            SearchRequest searchRequest = new SearchRequest(index)
                    .types(Question.QUESTION_COLLECTION_NAME)
                    .source(searchSourceBuilder);

            SearchResponse searchResponse = restHighLevelClient.search(searchRequest);
            SearchHits hits = searchResponse.getHits();
            Arrays.stream(hits.getHits()).forEach(h -> result.add(hitToDto(h)));
        } catch (IOException e) {
            log.warn("elasticSimpleSearch_Can't perform request to ES, with error {}", e.getMessage());
        }
        return new QuestionAllFieldsListDto((long) result.size(), result);
    }

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
                    .operator(Operator.OR);
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

    @SneakyThrows
    private QuestionAllFieldsDto hitToDto(SearchHit hit) {
        Map<String, Object> map = hit.getSource();

        QuestionAllFieldsDto questionAllFieldsDto = new QuestionAllFieldsDto();
        questionAllFieldsDto.setQuestionId(hit.getId());
        questionAllFieldsDto.setTitle((String) map.get("title"));

        String value = (String) map.get("authorId");
        Matcher m = dbRefPattern.matcher(value);
        m.matches();
        value = m.group(1);
        questionAllFieldsDto.setAuthor(userMapper.modelToBaseDto(userRepository.findByUserId(value).get()));

        value = (String) map.get("createdAt");
        value = value.substring(0, value.length() - 3); // "2017-11-15T08:49:28.106000" -> "2017-11-15T08:49:28.106"
        questionAllFieldsDto.setCreatedAt(new SimpleDateFormat(elsticDateTimePattern).parse(value).toInstant());

        return questionAllFieldsDto;
    }
}
