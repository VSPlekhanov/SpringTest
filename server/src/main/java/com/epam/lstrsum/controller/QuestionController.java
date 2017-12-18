package com.epam.lstrsum.controller;

import com.epam.lstrsum.dto.common.CounterDto;
import com.epam.lstrsum.dto.question.*;
import com.epam.lstrsum.service.QuestionService;
import com.epam.lstrsum.service.SearchQueryService;
import com.epam.lstrsum.service.UserService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.epam.lstrsum.enums.UserRoleType.ROLE_SIMPLE_USER;

@RestController
@RequestMapping("/api/question")
@ConfigurationProperties(prefix = "question")
@RequiredArgsConstructor
@Slf4j
public class QuestionController {

    private final QuestionService questionService;
    private final SearchQueryService queryService;
    private final UserService userService;
    private final UserRuntimeRequestComponent userRuntimeRequestComponent;
    private final ObjectMapper mapper;

    @Setter
    private int maxQuestionAmount;

    @PostMapping
    public ResponseEntity<String> addQuestion(@RequestPart("dtoObject") QuestionPostDto dtoObject,
            @RequestPart(value = "files", required = false) MultipartFile[] files)
            throws IOException {
        log.debug("addQuestion.enter; dtoObject: {}", dtoObject);

        String authorEmail = userRuntimeRequestComponent.getEmail();
        log.debug("addQuestion; email: {}", authorEmail);

        long usersAdded = userService.findUserByEmailIfExist(authorEmail)
                .map(u -> 0L)
                .orElseGet(() -> userService.addIfNotExistAllWithRole(Collections.singletonList(authorEmail), ROLE_SIMPLE_USER));

        usersAdded += userService.addIfNotExistAllWithRole(dtoObject.getAllowedSubs(), ROLE_SIMPLE_USER);
        log.debug("{} users added to db", usersAdded);

        List<String> allowedSubs = dtoObject.getAllowedSubs();
        if (!allowedSubs.contains(authorEmail)) allowedSubs.add(authorEmail);

        String questionId = questionService.addNewQuestion(dtoObject, authorEmail, files).getQuestionId();
        log.debug("addQuestion; question: {}", questionId);

        return ResponseEntity.ok(questionId);
    }

    @GetMapping(value = "/{questionId}")
    public ResponseEntity<QuestionAppearanceDto> getQuestionWithText(@PathVariable String questionId) {
        String userEmail = userRuntimeRequestComponent.getEmail();
        Optional<QuestionAppearanceDto> questionDto = currentUserInDistributionList() ?
                questionService.getQuestionAppearanceDtoByQuestionId(questionId, userEmail) :
                questionService.getQuestionAppearanceDtoByQuestionIdWithAllowedSub(questionId, userEmail);
        return questionDto.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/list")
    public ResponseEntity<QuestionWithAnswersCountListDto> getQuestions(
            @RequestParam(required = false, defaultValue = "-1") int questionPage,
            @RequestParam(required = false, defaultValue = "-1") int questionAmount) {

        if ((questionAmount > maxQuestionAmount) || (questionAmount <= 0)) {
            questionAmount = maxQuestionAmount;
        }

        boolean currentUserInDistributionList = currentUserInDistributionList();
        Long count = getTotalQuestionsCount(currentUserInDistributionList);

        if (questionAmount <= 0 || count <= 0L) {
            return ResponseEntity.ok(new QuestionWithAnswersCountListDto(count, Collections.emptyList()));
        }

        int lastPage = (count.intValue() - 1) / questionAmount;
        if (questionPage > lastPage) {
            questionPage = lastPage;
        }

        if (questionPage < 0) {
            questionPage = 0;
        }

        List<QuestionWithAnswersCountDto> questionsFromService = currentUserInDistributionList ?
                questionService.findAllQuestionsBaseDto(questionPage, questionAmount) :
                questionService.findAllQuestionBaseDtoWithAllowedSub(questionPage, questionAmount, userRuntimeRequestComponent.getEmail());

        return ResponseEntity.ok(new QuestionWithAnswersCountListDto(count, questionsFromService));
    }

    @Deprecated
    @GetMapping("/count")
    public ResponseEntity<CounterDto> getQuestionCount() {
        return ResponseEntity.ok().body(new CounterDto(getTotalQuestionsCount()));
    }

    @Deprecated
    private Long getTotalQuestionsCount() {
        return getTotalQuestionsCount(currentUserInDistributionList());
    }

    private Long getTotalQuestionsCount(boolean currentUserInDistributionList) {
        return currentUserInDistributionList ?
                questionService.getQuestionCount() :
                questionService.getQuestionCountWithAllowedSub(userRuntimeRequestComponent.getEmail());
    }

    @Deprecated
    @GetMapping("/searchMongo")
    public ResponseEntity<QuestionAllFieldsListDto> mongoSearch(
            @RequestParam("query") String query,
            @RequestParam(required = false, defaultValue = "-1") Integer page,
            @RequestParam(required = false, defaultValue = "-1") Integer size) {

        if ((size > maxQuestionAmount) || (size <= 0)) {
            size = maxQuestionAmount;
        }

        boolean currentUserInDistributionList = currentUserInDistributionList();
        Long count = getSearchCount(query, currentUserInDistributionList);

        if (size <= 0 || count <= 0L) {
            return ResponseEntity.ok(new QuestionAllFieldsListDto(count, Collections.emptyList()));
        }

        int lastPage = (count.intValue() - 1) / size;
        if (page > lastPage) {
            page = lastPage;
        }

        if (page < 0) {
            page = 0;
        }

        List<QuestionAllFieldsDto> questionDtoList = currentUserInDistributionList ?
                questionService.search(query, page, size) :
                questionService.searchWithAllowedSub(query, page, size, userRuntimeRequestComponent.getEmail());

        return ResponseEntity.ok(new QuestionAllFieldsListDto(count, questionDtoList));
    }

    @GetMapping("/search")
    public ResponseEntity<QuestionAllFieldsListDto> elasticSearch(
            @RequestParam("query") String query,
            @RequestParam(value = "page", required = false, defaultValue = "-1") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "-1") Integer size) {

        if ((size > maxQuestionAmount) || (size <= 0)) {
            size = maxQuestionAmount;
        }
        if ((page < 0)) {
            page = 0;
        }

        QuestionParsedQueryDto parsedQueryDto = queryService.parseQuery(query);

        if (parsedQueryDto.getErrorsInQuery().isEmpty()){
            QuestionAllFieldsListDto questionAllFieldsListDto = questionService.elasticSimpleSearch(
                            parsedQueryDto.getQueryForSearch(),
                            parsedQueryDto.getQueryStringsWithMetaTags(),
                            page,
                            size);
            return ResponseEntity.ok(questionAllFieldsListDto);
        } else {
            return ResponseEntity.ok(null);
        }
    }

    @Deprecated
    @GetMapping("/smartSearch")
    public ResponseEntity<String> smartSearch(
            @RequestParam("query") String query,
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "0") Integer size) {
        String searchResult = currentUserInDistributionList() ?
                questionService.smartSearch(query, page, size) :
                questionService.smartSearchWithAllowedSub(query, page, size, userRuntimeRequestComponent.getEmail());
        return ResponseEntity.ok(searchResult);
    }

    @Deprecated
    @GetMapping("/advancedSearch")
    public ResponseEntity<String> advancedSearch (
            @RequestParam("query") String query,
            @RequestParam(required = false, defaultValue = "-1") Integer page,
            @RequestParam(required = false, defaultValue = "-1") Integer size) {

        if ((size > maxQuestionAmount) || (size <= 0)) {
            size = maxQuestionAmount;
        }
        if ((page <= 0)) {
            page = 0;
        }

        QuestionParsedQueryDto parsedQueryDto = queryService.parseQuery(query);

        if (parsedQueryDto.getErrorsInQuery().isEmpty()){
            String resultValue = questionService.advancedSearch(
                    parsedQueryDto.getQueryForSearch(),
                    parsedQueryDto.getQueryStringsWithMetaTags(),
                    page,
                    size
            );
            return ResponseEntity.ok(resultValue);
        } else {
            JsonNode errorNode = mapper.createObjectNode();
            JsonNode indexesNode = mapper.valueToTree(parsedQueryDto.getErrorsInQuery());
            ((ObjectNode) errorNode).put("message", "Error in query parsing.");
            ((ObjectNode) errorNode).put("indexes", indexesNode);

            return ResponseEntity.badRequest().body(errorNode.toString());
        }
    }

    @Deprecated
    @GetMapping("/search/count")
    public ResponseEntity<CounterDto> searchCount(@RequestParam("query") String query) {
        Long count = getSearchCount(query);
        return ResponseEntity.ok()
                .body(new CounterDto(count));
    }

    private Long getSearchCount(@RequestParam("query") String query) {
        return getSearchCount(query, currentUserInDistributionList());
    }

    private Long getSearchCount(@RequestParam("query") String query, boolean currentUserInDistributionList) {
        return currentUserInDistributionList ?
                questionService.getTextSearchResultsCount(query) :
                questionService.getTextSearchResultsCountWithAllowedSub(query, userRuntimeRequestComponent.getEmail());
    }

    @GetMapping("/getRelevantTags")
    public ResponseEntity<List<String>> getRelevantTags(@RequestParam("key") String key) {
        return  ResponseEntity.ok(questionService.getRelevantTags(key));
    }

    private boolean currentUserInDistributionList() {
        return userRuntimeRequestComponent.isInDistributionList();
    }
}
