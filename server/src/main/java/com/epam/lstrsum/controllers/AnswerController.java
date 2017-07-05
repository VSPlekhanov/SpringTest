package com.epam.lstrsum.controllers;

import com.epam.lstrsum.model.Answer;
import com.epam.lstrsum.service.AnswerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/answer")
public class AnswerController {

    private final AnswerService answerService;

    @Autowired
    public AnswerController(AnswerService answerService) {
        this.answerService = answerService;
    }

    @RequestMapping
    public List<Answer> getListOfAnswers() {
        return answerService.findAll();
    }
}
