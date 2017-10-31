package com.epam.lstrsum.aggregators;

import com.epam.lstrsum.converter.AnswerDtoMapper;
import com.epam.lstrsum.converter.QuestionDtoMapper;
import com.epam.lstrsum.converter.UserDtoMapper;
import com.epam.lstrsum.converter.contract.AllFieldModelDtoConverter;
import com.epam.lstrsum.converter.contract.BasicModelDtoConverter;
import com.epam.lstrsum.dto.answer.AnswerAllFieldsDto;
import com.epam.lstrsum.dto.answer.AnswerBaseDto;
import com.epam.lstrsum.dto.answer.AnswerPostDto;
import com.epam.lstrsum.dto.user.UserBaseDto;
import com.epam.lstrsum.model.Answer;
import com.epam.lstrsum.model.Question;
import com.epam.lstrsum.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnswerAggregator implements BasicModelDtoConverter<Answer, AnswerBaseDto>,
        AllFieldModelDtoConverter<Answer, AnswerAllFieldsDto> {

    private final AnswerDtoMapper answerMapper;
    private final UserDtoMapper userMapper;
    private final QuestionDtoMapper questionMapper;

    private final MongoTemplate mongoTemplate;

    private final UserAggregator userAggregator;

    private final UserService userService;

    @Override
    public AnswerAllFieldsDto modelToAllFieldsDto(Answer answer) {
        final UserBaseDto author = userMapper.modelToBaseDto(userService.findUserById(answer.getAuthorId()));
        final Query query = new Query(Criteria.where("answers.answerId").is(answer.getAnswerId()));
        final Question question = mongoTemplate.findOne(query, Question.class);
        return answerMapper.modelToAllFieldsDto(
                answer,
                author,
                questionMapper.modelToBaseDto(
                        question,
                        author)
        );
    }

    @Override
    public AnswerBaseDto modelToBaseDto(Answer answer) {
        return answerMapper.modelToBaseDto(
                answer,
                userMapper.modelToBaseDto(userService.findUserById(answer.getAuthorId()))
        );
    }

    public List<AnswerBaseDto> answersToQuestionInAnswerBaseDto(Question question) {
        List<Answer> answers = question.getAnswers();
        return answerMapper.answersToQuestionInAnswerBaseDto(
                answers,
                userMapper.usersToListOfBaseDtos(answers.stream()
                        .map(Answer::getAuthorId).map(userService::findUserById).collect(Collectors.toList()))
        );
    }

    public Answer answerPostDtoAndAuthorEmailToAnswer(AnswerPostDto answerPostDto, String email) {
        return answerMapper.answerPostDtoAndAuthorEmailToAnswer(
                answerPostDto,
                userAggregator.findByEmail(email)
        );
    }
}
