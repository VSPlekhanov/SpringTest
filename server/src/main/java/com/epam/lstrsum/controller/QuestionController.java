package com.epam.lstrsum.controller;

import com.epam.lstrsum.dto.question.QuestionAllFieldsDto;
import com.epam.lstrsum.dto.question.QuestionAppearanceDto;
import com.epam.lstrsum.dto.question.QuestionBaseDto;
import com.epam.lstrsum.dto.question.QuestionPostDto;
import com.epam.lstrsum.service.QuestionService;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/question")
@ConfigurationProperties(prefix = "question")
@RequiredArgsConstructor
public class QuestionController {

    @Setter
    private int maxQuestionAmount;

    private final QuestionService questionService;
    private final UserRuntimeRequestComponent userRuntimeRequestComponent;

    @PostMapping
    public ResponseEntity<String> addQuestion(@RequestBody QuestionPostDto dtoObject)
            throws IOException {
        String email = userRuntimeRequestComponent.getEmail();
        String questionId = questionService.addNewQuestion(dtoObject, email).getQuestionId();
        return ResponseEntity.ok(questionId);
    }

    @GetMapping(value = "/{questionId}")
    public ResponseEntity<QuestionAppearanceDto> getQuestionWithAnswers(@PathVariable String questionId) {
        if (questionService.contains(questionId)) {
            QuestionAppearanceDto questionDto = questionService.getQuestionAppearanceDotByQuestionId(questionId);
            return ResponseEntity.ok(questionDto);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/list")
    public ResponseEntity<List<QuestionBaseDto>> getQuestions(
            @RequestParam(required = false, defaultValue = "-1") int questionPage,
            @RequestParam(required = false, defaultValue = "-1") int questionAmount) {
        if ((questionAmount > maxQuestionAmount) || (questionAmount <= 0)) {
            questionAmount = maxQuestionAmount;
        }
        if ((questionPage <= 0)) {
            questionPage = 0;
        }
        List<QuestionBaseDto> amountFrom = questionService.findAllQuestionsBaseDto(questionPage, questionAmount);
        return ResponseEntity.ok(amountFrom);
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getQuestionCount() {
        return ResponseEntity.ok().body(questionService.getQuestionCount());
    }

    @GetMapping("/search")
    public ResponseEntity<List<QuestionAllFieldsDto>> search(
            @RequestParam("query") String query,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size) {
        List<QuestionAllFieldsDto> questionDtoList = questionService.search(query, page, size);
        return ResponseEntity.ok(questionDtoList);
    }

    @GetMapping("/getTextSearchResultsCount")
    public ResponseEntity<Integer> searchCount(@RequestParam("query") String query) {
        Integer count  = questionService.getTextSearchResultsCount(query);

        return ResponseEntity.ok(count);
    }

    @GetMapping("/getRelevantTags")
    public ResponseEntity<List<String>> getRelevantTags(@RequestParam("key") String key) {
        return ResponseEntity.ok(questionService.getRelevantTags(key));
    }
}
