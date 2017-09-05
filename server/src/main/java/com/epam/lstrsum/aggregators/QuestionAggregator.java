package com.epam.lstrsum.aggregators;

import com.epam.lstrsum.converter.QuestionDtoMapper;
import com.epam.lstrsum.converter.UserDtoMapper;
import com.epam.lstrsum.converter.contract.AllFieldModelDtoConverter;
import com.epam.lstrsum.converter.contract.BasicModelDtoConverter;
import com.epam.lstrsum.dto.question.QuestionAllFieldsDto;
import com.epam.lstrsum.dto.question.QuestionAppearanceDto;
import com.epam.lstrsum.dto.question.QuestionBaseDto;
import com.epam.lstrsum.dto.question.QuestionPostDto;
import com.epam.lstrsum.dto.question.QuestionWithAnswersCountDto;
import com.epam.lstrsum.model.Question;
import com.epam.lstrsum.model.User;
import com.epam.lstrsum.model.QuestionWithAnswersCount;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

@Service
@RequiredArgsConstructor
public class QuestionAggregator implements BasicModelDtoConverter<Question, QuestionBaseDto>,
        AllFieldModelDtoConverter<Question, QuestionAllFieldsDto> {

    private final UserDtoMapper userMapper;
    private final QuestionDtoMapper questionMapper;

    private final AnswerAggregator answerAggregator;
    private final UserAggregator userAggregator;

    @Override
    public QuestionAllFieldsDto modelToAllFieldsDto(Question question) {
        return questionMapper.modelToAllFieldsDto(
                question,
                userMapper.modelToBaseDto(question.getAuthorId()),
                userMapper.usersToListOfUserBaseDtos(question.getAllowedSubs())
        );
    }

    @Override
    public QuestionBaseDto modelToBaseDto(Question question) {
        return questionMapper.modelToBaseDto(
                question,
                userMapper.modelToBaseDto(question.getAuthorId())
        );
    }

    public QuestionWithAnswersCountDto modelToAnswersCountDto(QuestionWithAnswersCount questionWithAnswersCount) {
        final Question source = questionWithAnswersCount.getQuestionId();

        return questionMapper.modelToAnswersCount(
                source,
                userMapper.modelToBaseDto(source.getAuthorId()),
                questionWithAnswersCount
        );
    }

    public QuestionAppearanceDto modelToQuestionAppearanceDto(Question question) {
        return questionMapper.modelToQuestionAppearanceDto(
                question,
                userMapper.modelToBaseDto(question.getAuthorId()),
                answerAggregator.answersToQuestionInAnswerBaseDto(question)
        );
    }

    public Question questionPostDtoAndAuthorEmailToQuestion(QuestionPostDto questionPostDto, String email) {
        return questionMapper.questionPostDtoAndAuthorEmailToQuestion(
                questionPostDto,
                userAggregator.findByEmail(email),
                getEmptyListIfNull(questionPostDto.getAllowedSubs())
        );
    }

    public List<QuestionBaseDto> subscriptionsToListOfQuestionBaseDto(List<Question> subscriptions) {
        return questionMapper.subscriptionsToListOfQuestionBaseDto(
                subscriptions,
                subscriptions.stream()
                        .map(Question::getAuthorId)
                        .map(userMapper::modelToBaseDto)
                        .collect(Collectors.toList())
        );
    }

    private List<User> getEmptyListIfNull(List<String> emails) {
        return isNull(emails) ? Collections.emptyList() :
                emails.stream()
                        .map(userAggregator::findByEmail)
                        .collect(Collectors.toList());
    }
}
