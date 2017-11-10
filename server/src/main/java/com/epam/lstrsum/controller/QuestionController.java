package com.epam.lstrsum.controller;

import com.epam.lstrsum.dto.common.CounterDto;
import com.epam.lstrsum.dto.question.QuestionAllFieldsDto;
import com.epam.lstrsum.dto.question.QuestionAppearanceDto;
import com.epam.lstrsum.dto.question.QuestionListDto;
import com.epam.lstrsum.dto.question.QuestionParsedQueryDto;
import com.epam.lstrsum.dto.question.QuestionPostDto;
import com.epam.lstrsum.dto.question.QuestionWithAnswersCountDto;
import com.epam.lstrsum.service.QuestionService;
import com.epam.lstrsum.service.SearchQueryService;
import com.epam.lstrsum.service.UserService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
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
        String email = userRuntimeRequestComponent.getEmail();
        if (!currentUserInDistributionList()) {
            log.warn("User with email : '" + email + "' try to add a new question. User isn't in the current distribution list!");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        val usersAdded = userService.addIfNotExistAllWithRole(dtoObject.getAllowedSubs(), ROLE_SIMPLE_USER);
        log.debug("{} users added", usersAdded);
        log.debug("addQuestion; email: {}", email);

        String questionId = questionService.addNewQuestion(dtoObject, email, files).getQuestionId();
        log.debug("addQuestion; question: {}", questionId);

        return ResponseEntity.ok(questionId);
    }

    @GetMapping(value = "/{questionId}")
    public ResponseEntity<QuestionAppearanceDto> getQuestionWithText(@PathVariable String questionId) {
        Optional<QuestionAppearanceDto> questionDto = currentUserInDistributionList() ?
                questionService.getQuestionAppearanceDtoByQuestionId(questionId, userRuntimeRequestComponent.getEmail()) :
                questionService.getQuestionAppearanceDtoByQuestionIdWithAllowedSub(questionId, userRuntimeRequestComponent.getEmail());
        return questionDto.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/list")
    public ResponseEntity<QuestionListDto> getQuestions(
            @RequestParam(required = false, defaultValue = "-1") int questionPage,
            @RequestParam(required = false, defaultValue = "-1") int questionAmount) {

        if ((questionAmount > maxQuestionAmount) || (questionAmount <= 0)) {
            questionAmount = maxQuestionAmount;
        }

        if (questionPage < 0) {
            questionPage = 0;
        }

        boolean currentUserInDistributionList = currentUserInDistributionList();
        Long count = getTotalQuestionsCount(currentUserInDistributionList);

        if (questionAmount == 0) {
            return ResponseEntity.ok(new QuestionListDto(count, Collections.emptyList()));
        }

        if (questionPage > count.intValue()) {
            questionPage = count.intValue() / questionAmount - 1;
        }

        List<QuestionWithAnswersCountDto> questionsFromService = currentUserInDistributionList ?
                questionService.findAllQuestionsBaseDto(questionPage, questionAmount) :
                questionService.findAllQuestionBaseDtoWithAllowedSub(questionPage, questionAmount, userRuntimeRequestComponent.getEmail());

        return ResponseEntity.ok(new QuestionListDto(count, questionsFromService));
    }

    @GetMapping("/count")
    public ResponseEntity<CounterDto> getQuestionCount() {
        return ResponseEntity.ok().body(new CounterDto(getTotalQuestionsCount()));
    }

    private Long getTotalQuestionsCount() {
        return getTotalQuestionsCount(currentUserInDistributionList());
    }

    private Long getTotalQuestionsCount(boolean currentUserInDistributionList) {
        return currentUserInDistributionList ?
                questionService.getQuestionCount() :
                questionService.getQuestionCountWithAllowedSub(userRuntimeRequestComponent.getEmail());
    }

    @GetMapping("/search")
    public ResponseEntity<List<QuestionAllFieldsDto>> search(
            @RequestParam("query") String query,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size) {
        List<QuestionAllFieldsDto> questionDtoList = currentUserInDistributionList() ?
                questionService.search(query, page, size) :
                questionService.searchWithAllowedSub(query, page, size, userRuntimeRequestComponent.getEmail());
        return ResponseEntity.ok(questionDtoList);
    }

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

    @GetMapping("/search/count")
    public ResponseEntity<CounterDto> searchCount(@RequestParam("query") String query) {
        Long count = currentUserInDistributionList() ?
                questionService.getTextSearchResultsCount(query) :
                questionService.getTextSearchResultsCountWithAllowedSub(query, userRuntimeRequestComponent.getEmail());
        return ResponseEntity.ok()
                .body(new CounterDto(count));
    }

    @GetMapping("/getRelevantTags")
    public ResponseEntity<List<String>> getRelevantTags(@RequestParam("key") String key) {
        return currentUserInDistributionList() ?
                ResponseEntity.ok(questionService.getRelevantTags(key)) :
                ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    private boolean currentUserInDistributionList() {
        return userRuntimeRequestComponent.isInDistributionList();
    }
}
