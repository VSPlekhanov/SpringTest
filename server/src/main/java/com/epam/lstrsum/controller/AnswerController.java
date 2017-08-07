package com.epam.lstrsum.controller;

import com.epam.lstrsum.annotation.NotEmptyString;
import com.epam.lstrsum.dto.answer.AnswerAllFieldsDto;
import com.epam.lstrsum.dto.answer.AnswerPostDto;
import com.epam.lstrsum.dto.vote.VoteAllFieldsDto;
import com.epam.lstrsum.service.AnswerService;
import com.epam.lstrsum.service.VoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/answer")
@RequiredArgsConstructor
public class AnswerController {

    private final AnswerService answerService;
    private final VoteService voteService;
    private final UserRuntimeRequestComponent userRuntimeRequestComponent;

    @PostMapping
    public ResponseEntity<AnswerAllFieldsDto> addAnswer(@RequestBody AnswerPostDto dtoObject)
            throws IOException {
        String email = userRuntimeRequestComponent.getEmail();
        AnswerAllFieldsDto answerAllFieldsDto = answerService.addNewAnswer(dtoObject, email);
        return ResponseEntity.ok(answerAllFieldsDto);
    }

    @PostMapping("/vote/{answerId}")
    public ResponseEntity<VoteAllFieldsDto> addVote(@NotEmptyString @PathVariable(value = "answerId") final String answerId) {
        String email = userRuntimeRequestComponent.getEmail();
        VoteAllFieldsDto voteAllFieldsDto = voteService.addVoteToAnswer(email, answerId);
        return ResponseEntity.ok(voteAllFieldsDto);
    }

    @PutMapping("/vote/{answerId}")
    public ResponseEntity<Boolean> deleteVote(@NotEmptyString @PathVariable(value = "answerId") final String answerId) {
        String email = userRuntimeRequestComponent.getEmail();
        voteService.deleteVoteToAnswer(email, answerId);
        return ResponseEntity.ok(true);
    }

    @GetMapping("/vote/all/{answerId}")
    public ResponseEntity<List<VoteAllFieldsDto>> getAllAnswerVotes(@NotEmptyString @PathVariable(value = "answerId") final String answerId) {
        List<VoteAllFieldsDto> allVotesForAnswer = voteService.findAllVotesForAnswer(answerId);
        return ResponseEntity.ok(allVotesForAnswer);
    }
}
