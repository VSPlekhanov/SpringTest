package com.epam.lstrsum.controller;


import com.epam.lstrsum.service.AnswerService;
import com.epam.lstrsum.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private final QuestionService questionService;
    private final AnswerService answerService;

    @DeleteMapping(value = "/question/{questionId}")
    public ResponseEntity deleteQuestionWithAnswers(@PathVariable String questionId) {
        if (!questionService.contains(questionId)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        questionService.delete(questionId);
        answerService.deleteAllAnswersOnQuestion(questionId);

        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }
}
