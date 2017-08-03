package com.epam.lstrsum.converter;

import com.epam.lstrsum.dto.answer.AnswerAllFieldsDto;
import com.epam.lstrsum.dto.answer.AnswerBaseDto;
import com.epam.lstrsum.dto.answer.AnswerPostDto;
import com.epam.lstrsum.model.Answer;
import com.epam.lstrsum.model.Question;
import com.epam.lstrsum.service.AnswerService;
import com.epam.lstrsum.service.QuestionService;
import com.epam.lstrsum.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnswerDtoConverter implements BasicModelDtoConverter<Answer, AnswerBaseDto>,
        AllFieldModelDtoConverter<Answer, AnswerAllFieldsDto> {

    private final UserService userService;
    private final AnswerService answerService;
    private final QuestionService questionService;

    @Override
    public AnswerAllFieldsDto modelToAllFieldsDto(Answer answer) {
        return new AnswerAllFieldsDto(answer.getText(), answer.getCreatedAt(),
                userService.modelToBaseDto(answer.getAuthorId()), answer.getUpVote(),
                answer.getAnswerId(), questionService.modelToBaseDto(answer.getParentId()));
    }

    @Override
    public AnswerBaseDto modelToBaseDto(Answer answer) {
        return new AnswerBaseDto(answer.getText(), answer.getCreatedAt(),
                userService.modelToBaseDto(answer.getAuthorId()), answer.getUpVote());
    }

    public List<AnswerBaseDto> answersToQuestionInAnswerBaseDto(Question question) {
        return answerService.findAnswersToThis(question).stream()
                .map(this::modelToBaseDto)
                .collect(Collectors.toList());
    }

    public Answer answerPostDtoAndAuthorEmailToAnswer(AnswerPostDto answerPostDto, String email) {
        return Answer.builder()
                .upVote(0)
                .text(answerPostDto.getText())
                .createdAt(Instant.now())
                .parentId(questionService.getQuestionById(answerPostDto.getParentId()))
                .authorId(userService.getUserByEmail(email))
                .build();
    }

}
