package com.epam.lstrsum.controller;

import com.epam.lstrsum.annotation.NotEmptyString;
import com.epam.lstrsum.dto.answer.AnswerAllFieldsDto;
import com.epam.lstrsum.dto.answer.AnswerBaseDto;
import com.epam.lstrsum.dto.answer.AnswerPostDto;
import com.epam.lstrsum.dto.common.CounterDto;
import com.epam.lstrsum.service.AnswerService;
import com.epam.lstrsum.service.VoteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/answer")
@RequiredArgsConstructor
@Validated
@Slf4j
public class AnswerController {

    private final AnswerService answerService;
    private final UserRuntimeRequestComponent userRuntimeRequestComponent;
    private final VoteService voteService;

    @PostMapping
    public ResponseEntity<AnswerAllFieldsDto> addAnswer(@Validated @RequestBody AnswerPostDto dtoObject)
            throws IOException {
        String email = userRuntimeRequestComponent.getEmail();
        AnswerAllFieldsDto answerAllFieldsDto = currentUserInDistributionList() ?
                answerService.addNewAnswer(dtoObject, email) :
                answerService.addNewAnswerWithAllowedSub(dtoObject, email);
        return ResponseEntity.ok(answerAllFieldsDto);
    }

    @PutMapping("/vote")
    public ResponseEntity voteFor(@NotEmptyString @RequestParam String answerId) {
        String email = userRuntimeRequestComponent.getEmail();
        boolean successVote = currentUserInDistributionList() ?
                voteService.voteForAnswerByUser(answerId, email) :
                voteService.voteForAnswerByAllowedSub(answerId, email);

        return successVote ? ResponseEntity.status(HttpStatus.NO_CONTENT).build() : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @PutMapping("/unvote")
    public ResponseEntity unvoteFor(@NotEmptyString @RequestParam String answerId) {
        String email = userRuntimeRequestComponent.getEmail();
        boolean successUnvote = currentUserInDistributionList() ?
                voteService.unvoteForAnswerByUser(answerId, email) :
                voteService.unvoteForAnswerByAllowedSub(answerId, email);

        return successUnvote ? ResponseEntity.status(HttpStatus.NO_CONTENT).build() : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @GetMapping("/{questionId}")
    public ResponseEntity<List<AnswerBaseDto>> getAnswersByQuestionId(
            @PathVariable String questionId,
            @RequestParam(value = "page", required = false, defaultValue = "-1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "-1") int size) {
        return ResponseEntity.ok(answerService.getAnswersByQuestionId(questionId, page, size));
    }

    @GetMapping("/{questionId}/count")
    public ResponseEntity<CounterDto> getAnswerCountByQuestionId(@NotEmptyString @PathVariable String questionId) {
        return ResponseEntity.ok().body(new CounterDto(answerService.getAnswerCountByQuestionId(questionId)));
    }

    private boolean currentUserInDistributionList() {
        return userRuntimeRequestComponent.isInDistributionList();
    }
}
