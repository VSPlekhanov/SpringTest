package com.epam.lstrsum.service.impl;

import com.epam.lstrsum.controller.UserRuntimeRequestComponent;
import com.epam.lstrsum.converter.UserDtoMapper;
import com.epam.lstrsum.dto.question.QuestionWithAnswersCountDto;
import com.epam.lstrsum.dto.question.QuestionWithAnswersCountListDto;
import com.epam.lstrsum.dto.question.QuestionWithHighlightersDto;
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
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.toList;

@Service
@Slf4j
@ConfigurationProperties(prefix = "elastic")
@RequiredArgsConstructor
public class ElasticSearchServiceImpl implements ElasticSearchService {

    // Pattern for getting id from string like "DBRef(u'User', ObjectId('5a1571563867e59abcba48ca'))"
    private final Pattern dbRefPattern = Pattern.compile("^.*\\'(\\w*)\\'\\)\\)$");
    private final String elasticDateTimePattern = "yyyy-MM-dd'T'HH:mm:ss.SSS";
    private final int highlightedFragmentSize = 150;

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
    public QuestionWithAnswersCountListDto advancedSearchGetDtoWithHighlights(String searchString, List<String> metaTags, Integer page, Integer size) {
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
            Arrays.stream(hits.getHits()).forEach(h -> result.add(hitToDtoWithHighlights(h)));
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
        highlightBuilder
                .field(new HighlightBuilder.Field("title").numOfFragments(0))
                .field(new HighlightBuilder.Field("text")
                        .fragmentSize(highlightedFragmentSize)
                        .noMatchSize(highlightedFragmentSize))
                .field(new HighlightBuilder.Field("tags"));
        return highlightBuilder;
    }

    @SneakyThrows
    private QuestionWithAnswersCountDto hitToDtoWithAnswersCount(SearchHit hit) {
        Map<String, Object> map = hit.getSource();

        QuestionWithAnswersCountDto questionDto = new QuestionWithAnswersCountDto();
        String questionId = hit.getId();

        questionDto.setQuestionId(questionId);
        questionDto.setTitle((String) map.get("title"));
        fillDtoFromMapWithAuthor(map, questionDto);
        fillDtoFromMapWithCreatedDate(map, questionDto);
        fillDtoFromMapWithTags(map, questionDto, questionId);
        fillDtoFromMapWithAnswers(map, questionDto, questionId);

        return questionDto;
    }

    private void fillDtoFromMapWithAnswers(Map<String, Object> map, QuestionWithAnswersCountDto questionDto, String questionId) {
        List list = getFieldFromSearchHit(map, "answers", questionId);
        questionDto.setAnswersCount(Objects.nonNull(list) ? list.size() : 0);
    }

    @SuppressWarnings("unchecked")
    private void fillDtoFromMapWithTags(Map<String, Object> map, QuestionWithAnswersCountDto questionDto, String questionId) {
        List list = getFieldFromSearchHit(map, "tags", questionId);
        if (Objects.nonNull(list)) {
            List<String> tags = (List<String>)list;
            questionDto.setTags(tags.toArray(new String[tags.size()]));
        }
    }

    private void fillDtoFromMapWithAuthor(Map<String, Object> map, QuestionWithAnswersCountDto questionDto) {
        String authorId = (String) map.get("authorId");
        Matcher m = dbRefPattern.matcher(authorId);
        if (m.matches()) {
            User user = userService.findUserById(m.group(1));
            questionDto.setAuthor(userMapper.modelToBaseDto(user));
        }
    }

    private void fillDtoFromMapWithCreatedDate(Map<String, Object> map, QuestionWithAnswersCountDto questionDto) throws ParseException {
        String createdAt = (String) map.get("createdAt");
        createdAt = createdAt.substring(0, createdAt.length() - 3);
        questionDto.setCreatedAt(new SimpleDateFormat(elasticDateTimePattern).parse(createdAt).toInstant()); // "2017-11-15T08:49:28.106000" -> "2017-11-15T08:49:28.106"
    }

    @SneakyThrows
    private QuestionWithAnswersCountDto hitToDtoWithHighlights(SearchHit hit) {
        Map<String, Object> source = hit.getSource();

        QuestionWithHighlightersDto questionDto = new QuestionWithHighlightersDto();
        String questionId = hit.getId();
        questionDto.setQuestionId(questionId);
        fillDtoFromMapWithAuthor(source, questionDto);
        fillDtoFromMapWithCreatedDate(source, questionDto);
        fillDtoFromMapWithAnswers(source, questionDto, questionId);

        Map<String, HighlightField> highlights = hit.getHighlightFields();

        String title = highlights.containsKey("title") ? highlights.get("title").getFragments()[0].string() : (String) source.get("title");
        questionDto.setTitle(title);

        fillDtoFromMapWithHighlightedTags(source, questionDto, questionId, highlights);

        fillDtoFromMapWithHighlightedText(questionDto, highlights);

        return questionDto;
    }

    private void fillDtoFromMapWithHighlightedText(QuestionWithHighlightersDto questionDto, Map<String, HighlightField> highlights) {
        if (highlights.containsKey("text")){
            String[] highlightedText = Arrays.stream(highlights.get("text").getFragments())
                    .map(Text::string)
                    .toArray(String[]::new);
            questionDto.setHighlightedText(highlightedText);
        }
    }

    @SuppressWarnings("unchecked")
    private void fillDtoFromMapWithHighlightedTags(Map<String, Object> source, QuestionWithHighlightersDto questionDto, String questionId, Map<String, HighlightField> highlights) {
        List list = getFieldFromSearchHit(source, "tags", questionId);

        if (Objects.nonNull(list)) {
            String[] resultTags;
            List<String> tags = (List<String>) list;

            if (highlights.containsKey("tags")) {
                List<String> highlightedTags = Arrays.stream(highlights.get("tags").getFragments())
                        .map(Text::string)
                        .collect(toList());

                resultTags = tags.stream()
                        .map(t -> {
                               for (String ht: highlightedTags){
                                   if (t.equals(ht.substring(4, ht.length() - 5))) {
                                       return ht;
                                   }
                               }
                               return t;
                        })
                        .toArray(String[]::new);
            } else {
                resultTags = tags.toArray(new String[tags.size()]);
            }

            questionDto.setTags(resultTags);
        }
    }

    private List getFieldFromSearchHit(Map<String, Object> map, String key, String id){
        List result = null;

        Object object = map.get(key);
        if (!Objects.nonNull(object)) {
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
