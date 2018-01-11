package com.epam.lstrsum.service.impl;

import com.epam.lstrsum.controller.UserRuntimeRequestComponent;
import com.epam.lstrsum.converter.UserDtoMapper;
import com.epam.lstrsum.dto.question.QuestionWithAnswersCountDto;
import com.epam.lstrsum.dto.question.QuestionWithAnswersCountListDto;
import com.epam.lstrsum.model.Question;
import com.epam.lstrsum.model.User;
import com.epam.lstrsum.persistence.UserRepository;
import com.epam.lstrsum.service.ElasticSearchService;
import com.epam.lstrsum.service.UserService;
import com.epam.lstrsum.utils.MessagesHelper;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.index.query.*;
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

    @Autowired
    private MessagesHelper messagesHelper;

    @Autowired
    private UserRuntimeRequestComponent userRuntimeRequestComponent;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private UserDtoMapper userMapper;

    @Setter
    private List<String> validMetaTags;


    private SearchResponse getElasticSearchResponse(String searchString,
                                                    List<String> metaTags,
                                                    Integer page,
                                                    Integer size,
                                                    String[] includeFields,
                                                    String[] excludeFields) throws IOException {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                .query(createQuery(searchString, metaTags))
                .from(page*size)
                .size(size)
                .sort("createdAt", SortOrder.DESC)
                .highlighter(createHighlighter());
        searchSourceBuilder.fetchSource(includeFields, excludeFields);
        SearchRequest searchRequest = new SearchRequest(index)
                .types(Question.QUESTION_COLLECTION_NAME)
                .source(searchSourceBuilder);
        return restHighLevelClient.search(searchRequest);
    }

    @Override
    public QuestionWithAnswersCountListDto advancedSearchGetDto(String searchString, List<String> metaTags, Integer page, Integer size) {
        List<QuestionWithAnswersCountDto> result = new ArrayList<>();
        long totalCount = 0L;

        try {
            SearchResponse searchResponse = getElasticSearchResponse(
                    searchString,
                    metaTags,
                    page,
                    size,
                    new String[]{"title", "createdAt", "authorId", "tags", "answers"},
                    new String[]{""}
                    );
            SearchHits hits = searchResponse.getHits();
            totalCount = hits.getTotalHits();
            Arrays.stream(hits.getHits()).forEach(h -> result.add(hitToDtoWithAnswersCount(h)));
        } catch (IOException e) {
            log.warn("advancedSearchGetDto_Can't perform request to ES, with error {}", e.getMessage());
        }
        return new QuestionWithAnswersCountListDto(totalCount, result);
    }

    @Deprecated
    @Override
    public String advancedSearchGetString(String searchString, List<String> metaTags, Integer page, Integer size) {
        try {
            SearchResponse searchResponse = getElasticSearchResponse(searchString, metaTags, page, size, null, null);
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
        if (!userRuntimeRequestComponent.isInDistributionList()) {
            String currentUserId = userRepository.findByEmailIgnoreCase(userRuntimeRequestComponent.getEmail()).get().getUserId();
            query.filter(getUserPermissionFilter(currentUserId));
        }

        if (!metaTags.isEmpty()) {
            query.filter(createFilter(metaTags));
        }

        return query;
    }

    private BoolQueryBuilder getUserPermissionFilter(String currentUserId) {
        return QueryBuilders.boolQuery()
                            .should(QueryBuilders.termQuery("authorId", currentUserId))
                            .should(QueryBuilders.termQuery("allowedSubs", currentUserId));
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
    private QuestionWithAnswersCountDto hitToDtoWithAnswersCount(SearchHit hit) {
        Map<String, Object> map = hit.getSource();

        QuestionWithAnswersCountDto questionDto = new QuestionWithAnswersCountDto();
        String questionId = hit.getId();
        questionDto.setQuestionId(questionId);
        questionDto.setTitle((String) map.get("title"));

        String authorId = (String) map.get("authorId");
        Matcher m = dbRefPattern.matcher(authorId);
        if (m.matches()) {
            User user = userService.findUserById(m.group(1));
            questionDto.setAuthor(userMapper.modelToBaseDto(user));
        }
        String createdAt = (String) map.get("createdAt");
        createdAt = createdAt.substring(0, createdAt.length() - 3);
        questionDto.setCreatedAt(new SimpleDateFormat(elsticDateTimePattern).parse(createdAt).toInstant()); // "2017-11-15T08:49:28.106000" -> "2017-11-15T08:49:28.106"

        List list = getFieldFromSearchHit(map, "tags", questionId);
        if (list != null) {
            List<String> tags = (List<String>)list;
            questionDto.setTags(tags.toArray(new String[tags.size()]));
        }

        list = getFieldFromSearchHit(map, "answers", questionId);
        questionDto.setAnswersCount(list != null ? list.size() : 0);

        return questionDto;
    }

    private List getFieldFromSearchHit(Map<String, Object> map, String key, String id){
        List result = null;

        Object object = map.get(key);
        if (object == null) {
            log.error("ElasticSearch response includes question (id = {}) without {} field.", id, key);
        } else {
            if (object instanceof List<?>) {
                result = (List) object;
            } else {
                log.error("ElasticSearch response includes question (id = {}) with {} in undefined structure.", id, key);
            }
        }
        return result;
    }
}
