package com.epam.lstrsum.controller;

import com.epam.lstrsum.dto.answer.AnswerAllFieldsDto;
import com.epam.lstrsum.dto.answer.AnswerPostDto;
import com.epam.lstrsum.model.Answer;
import com.epam.lstrsum.security.EpamEmployeePrincipal;
import com.epam.lstrsum.service.AnswerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/answer")
public class AnswerController {
    @Autowired
    private AnswerService answerService;

    @Autowired
    public AnswerController(AnswerService answerService) {
        this.answerService = answerService;
    }

    @PostMapping()
    public ResponseEntity<AnswerAllFieldsDto> addAnswer(Authentication authentication, @RequestBody() AnswerPostDto dtoObject)
            throws IOException {
        String email = authentication != null ? ((EpamEmployeePrincipal) (authentication.getPrincipal())).getEmail() : "John_Doe@epam.com";
        Answer answer = answerService.addNewAnswer(dtoObject, email);
        AnswerAllFieldsDto answerAllFieldsDto = answerService.answerToDto(answer);
        return ResponseEntity.ok(answerAllFieldsDto);
    }
}
