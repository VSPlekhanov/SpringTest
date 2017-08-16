package com.epam.lstrsum.service.impl;

import com.epam.lstrsum.aggregators.AnswerAggregator;
import com.epam.lstrsum.dto.answer.AnswerAllFieldsDto;
import com.epam.lstrsum.dto.answer.AnswerPostDto;
import com.epam.lstrsum.email.EmailNotification;
import com.epam.lstrsum.email.template.NewAnswerNotificationTemplate;
import com.epam.lstrsum.exception.AnswerValidationException;
import com.epam.lstrsum.exception.NoSuchAnswerException;
import com.epam.lstrsum.model.Answer;
import com.epam.lstrsum.persistence.AnswerRepository;
import com.epam.lstrsum.persistence.QuestionRepository;
import com.epam.lstrsum.persistence.UserRepository;
import com.epam.lstrsum.service.AnswerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static java.util.Objects.isNull;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnswerServiceImpl implements AnswerService {

    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;

    private final AnswerAggregator answerAggregator;

    @Override
    @EmailNotification(template = NewAnswerNotificationTemplate.class)
    public AnswerAllFieldsDto addNewAnswer(AnswerPostDto answerPostDto, String email) {
        log.debug("Add new answer with email {}", email);
        validateAnswerData(answerPostDto, email);
        Answer newAnswer = answerAggregator.answerPostDtoAndAuthorEmailToAnswer(answerPostDto, email);
        Answer saved = answerRepository.save(newAnswer);
        return answerAggregator.modelToAllFieldsDto(saved);
    }

    @Override
    public void deleteAllAnswersOnQuestion(String questionId) {
        log.debug("Delete all answers on question with id {}", questionId);
        answerRepository.deleteAllByQuestionId_QuestionId(questionId);
    }

    private void validateAnswerData(AnswerPostDto answerPostDto, String email) {
        if (isNull(answerPostDto)) {
            throw new AnswerValidationException("Answer must be not null!");
        }
        if (isNull(email) || email.trim().isEmpty()) {
            throw new AnswerValidationException("Author must be not null or empty!");
        } else if (!userRepository.findByEmail(email).isPresent()) {
            throw new AnswerValidationException("No such user!");
        }
        if (isNull(answerPostDto.getText()) || answerPostDto.getText().trim().isEmpty()) {
            throw new AnswerValidationException("Null or empty fields found in answer " + answerPostDto.toJson());
        }
        if (isNull(answerPostDto.getQuestionId()) || answerPostDto.getQuestionId().trim().isEmpty()) {
            throw new AnswerValidationException("Parent is null or empty" + answerPostDto.getQuestionId());
        } else if (isNull(questionRepository.findOne(answerPostDto.getQuestionId()))) {
            throw new AnswerValidationException("No such question!");
        }
    }

    public Answer getAnswerById(String answerId) {
        return Optional.ofNullable(answerRepository.findOne(answerId)).orElseThrow(() -> new NoSuchAnswerException("No such Answer in user Collection"));
    }

    public void save(Answer answer) {
        log.debug("Saved answer with id {}", answer.getAnswerId());
        answerRepository.save(answer);
    }
}
