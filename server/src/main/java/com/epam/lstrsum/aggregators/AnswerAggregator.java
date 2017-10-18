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
import com.epam.lstrsum.persistence.AnswerRepository;
import com.epam.lstrsum.persistence.QuestionRepository;
import lombok.RequiredArgsConstructor;
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

    private final AnswerRepository answerRepository;
    private final QuestionRepository questionRepository;

    private final UserAggregator userAggregator;

    @Override
    public AnswerAllFieldsDto modelToAllFieldsDto(Answer answer) {
        final UserBaseDto author = userMapper.modelToBaseDto(answer.getAuthorId());
        return answerMapper.modelToAllFieldsDto(
                answer,
                author,
                questionMapper.modelToBaseDto(answer.getQuestionId(), author)
        );
    }

    @Override
    public AnswerBaseDto modelToBaseDto(Answer answer) {
        return answerMapper.modelToBaseDto(
                answer,
                userMapper.modelToBaseDto(answer.getAuthorId())
        );
    }

    public List<AnswerBaseDto> answersToQuestionInAnswerBaseDto(Question question) {
        List<Answer> answers = answerRepository.findAnswersByQuestionIdOrderByCreatedAtAsc(question);
        return answerMapper.answersToQuestionInAnswerBaseDto(
                answers,
                userMapper.usersToListOfBaseDtos(answers.stream().map(Answer::getAuthorId).collect(Collectors.toList()))
        );
    }

    public Answer answerPostDtoAndAuthorEmailToAnswer(AnswerPostDto answerPostDto, String email) {
        return answerMapper.answerPostDtoAndAuthorEmailToAnswer(
                answerPostDto,
                userAggregator.findByEmail(email),
                questionRepository.findOne(answerPostDto.getQuestionId())
        );
    }
}
