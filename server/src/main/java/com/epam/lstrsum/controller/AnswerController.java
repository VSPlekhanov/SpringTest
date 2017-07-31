package com.epam.lstrsum.controller;

import com.epam.lstrsum.dto.answer.AnswerAllFieldsDto;
import com.epam.lstrsum.dto.answer.AnswerPostDto;
import com.epam.lstrsum.model.Answer;
import com.epam.lstrsum.service.AnswerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/answer")
@RequiredArgsConstructor
public class AnswerController {

    private final AnswerService answerService;
    private final UserRuntimeRequestComponent userRuntimeRequestComponent;

    @PostMapping()
    public ResponseEntity<AnswerAllFieldsDto> addAnswer(@RequestBody() AnswerPostDto dtoObject)
            throws IOException {
        String email = userRuntimeRequestComponent.getEmail();
        AnswerAllFieldsDto answerAllFieldsDto = answerService.addNewAnswer(dtoObject, email);
        return ResponseEntity.ok(answerAllFieldsDto);
    }
}
