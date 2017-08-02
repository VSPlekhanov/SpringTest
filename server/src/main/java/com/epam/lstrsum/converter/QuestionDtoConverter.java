package com.epam.lstrsum.converter;


import com.epam.lstrsum.dto.question.QuestionAllFieldsDto;
import com.epam.lstrsum.dto.question.QuestionAppearanceDto;
import com.epam.lstrsum.dto.question.QuestionBaseDto;
import com.epam.lstrsum.dto.question.QuestionPostDto;
import com.epam.lstrsum.model.Question;
import com.epam.lstrsum.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuestionDtoConverter implements BasicModelDtoConverter<Question, QuestionBaseDto>,
        AllFieldModelDtoConverter<Question, QuestionAllFieldsDto> {

    @Autowired
    private UserService userService;
    @Autowired
    private UserDtoConverter userConverter;
    @Autowired
    private AnswerDtoConverter answerConverter;

    @Override
    public QuestionAllFieldsDto modelToAllFieldsDto(Question question) {
        return new QuestionAllFieldsDto(question.getQuestionId(), question.getTitle(),
                question.getTags(), question.getCreatedAt(), question.getDeadLine(),
                userConverter.modelToBaseDto(question.getAuthorId()), question.getUpVote(),
                userConverter.allowedSubsToListOfUserBaseDtos(question.getAllowedSubs()), question.getText());
    }

    @Override
    public QuestionBaseDto modelToBaseDto(Question question) {
        return new QuestionBaseDto(question.getQuestionId(), question.getTitle(),
                question.getTags(), question.getCreatedAt(), question.getDeadLine(),
                userConverter.modelToBaseDto(question.getAuthorId()), question.getUpVote());
    }

    public QuestionAppearanceDto modelToQuestionAppearanceDto(Question question) {
        return new QuestionAppearanceDto(question.getQuestionId(), question.getTitle(),
                question.getTags(), question.getCreatedAt(), question.getDeadLine(),
                userConverter.modelToBaseDto(question.getAuthorId()), question.getUpVote(),
                question.getText(), answerConverter.answersToQuestionInAnswerBaseDto(question));
    }

    public Question questionPostDtoAndAuthorEmailToQuestion(QuestionPostDto questionPostDto, String email) {
        return Question.builder()
                .title(questionPostDto.getTitle())
                .tags(questionPostDto.getTags())
                .text(questionPostDto.getText())
                .createdAt(Instant.now())
                // Instant can parse only this format of date "2017-11-29T10:15:30Z"
                // throws DateTimeException if QuestionPostDto got wrong data format
                .deadLine(Instant.ofEpochMilli(questionPostDto.getDeadLine()))
                .authorId(userService.getUserByEmail(email))
                .allowedSubs(questionPostDto.getAllowedSubs().stream()
                        .map(userEmail -> userService.getUserByEmail(userEmail))
                        .collect(Collectors.toList()))
                .upVote(0)
                .build();
    }

    public List<QuestionBaseDto> subscriptionsToListOfQuestionBaseDto(List<Question> subscriptions) {
        return subscriptions.stream()
                .map(this::modelToBaseDto)
                .collect(Collectors.toList());
    }
}

