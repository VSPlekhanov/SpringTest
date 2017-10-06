package com.epam.lstrsum.controller;

import com.epam.lstrsum.dto.common.CounterDto;
import com.epam.lstrsum.dto.question.QuestionAllFieldsDto;
import com.epam.lstrsum.dto.question.QuestionAppearanceDto;
import com.epam.lstrsum.dto.question.QuestionPostDto;
import com.epam.lstrsum.dto.question.QuestionWithAnswersCountDto;
import com.epam.lstrsum.enums.UserRoleType;
import com.epam.lstrsum.service.AttachmentService;
import com.epam.lstrsum.service.QuestionService;
import com.epam.lstrsum.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.nonNull;

@RestController
@RequestMapping("/api/question")
@ConfigurationProperties(prefix = "question")
@RequiredArgsConstructor
@Slf4j
public class QuestionController {

    private final QuestionService questionService;
    private final UserService userService;
    private final UserRuntimeRequestComponent userRuntimeRequestComponent;
    @Setter
    private int maxQuestionAmount;

    @PostMapping
    public ResponseEntity<String> addQuestion(@RequestPart("dtoObject") QuestionPostDto dtoObject, @RequestPart("files") MultipartFile[] files)
            throws IOException {
        log.debug("addQuestion.enter; dtoObject: {}", dtoObject);
        String email = userRuntimeRequestComponent.getEmail();

        final long usersAdded = userService.addIfNotExistAllWithRole(
                dtoObject.getAllowedSubs(), Collections.singletonList(UserRoleType.ROLE_SIMPLE_USER)
        );
        log.debug("{} users added", usersAdded);
        log.debug("addQuestion; email: {}", email);

        String questionId = questionService.addNewQuestion(dtoObject, email, files).getQuestionId();
        log.debug("addQuestion; questionId: {}", questionId);

        return ResponseEntity.ok(questionId);
    }

    @GetMapping(value = "/{questionId}")
    public ResponseEntity<QuestionAppearanceDto> getQuestionWithText(@PathVariable String questionId) {
        Optional<QuestionAppearanceDto> questionDto = questionService.getQuestionAppearanceDtoByQuestionId(questionId);
        return questionDto.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/list")
    public ResponseEntity<List<QuestionWithAnswersCountDto>> getQuestions(
            @RequestParam(required = false, defaultValue = "-1") int questionPage,
            @RequestParam(required = false, defaultValue = "-1") int questionAmount) {

        if ((questionAmount > maxQuestionAmount) || (questionAmount <= 0)) {
            questionAmount = maxQuestionAmount;
        }
        if ((questionPage <= 0)) {
            questionPage = 0;
        }

        List<QuestionWithAnswersCountDto> questionsFromService = currentUserInDistributionList() ?
                questionService.findAllQuestionsBaseDto(questionPage, questionAmount) :
                questionService.findAllQuestionBaseDtoWithAllowedSub(questionPage, questionAmount, userRuntimeRequestComponent.getEmail());

        return ResponseEntity.ok(questionsFromService);
    }

    private boolean currentUserInDistributionList() {
        return userRuntimeRequestComponent.isInDistributionList();
    }

    @GetMapping("/count")
    public ResponseEntity<CounterDto> getQuestionCount() {
        return ResponseEntity.ok().body(new CounterDto(questionService.getQuestionCount()));
    }

    @GetMapping("/search")
    public ResponseEntity<List<QuestionAllFieldsDto>> search(
            @RequestParam("query") String query,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size) {
        List<QuestionAllFieldsDto> questionDtoList = questionService.search(query, page, size);
        return ResponseEntity.ok(questionDtoList);
    }

    @GetMapping("/smartSearch")
    public ResponseEntity<String> smartSearch(
            @RequestParam("query") String query,
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "0") Integer size) {
        return ResponseEntity.ok(questionService.smartSearch(query, page, size));
    }

    @GetMapping("/search/count")
    public ResponseEntity<CounterDto> searchCount(@RequestParam("query") String query) {
        return ResponseEntity.ok()
                .body(new CounterDto(questionService.getTextSearchResultsCount(query)));
    }

    @GetMapping("/getRelevantTags")
    public ResponseEntity<List<String>> getRelevantTags(@RequestParam("key") String key) {
        return ResponseEntity.ok(questionService.getRelevantTags(key));
    }
}
