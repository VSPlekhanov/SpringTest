package com.epam.lstrsum.service.impl;

import com.epam.lstrsum.aggregators.AnswerAggregator;
import com.epam.lstrsum.dto.answer.AnswerAllFieldsDto;
import com.epam.lstrsum.dto.answer.AnswerBaseDto;
import com.epam.lstrsum.dto.answer.AnswerPostDto;
import com.epam.lstrsum.email.EmailNotification;
import com.epam.lstrsum.email.template.NewAnswerNotificationTemplate;
import com.epam.lstrsum.exception.BusinessLogicException;
import com.epam.lstrsum.exception.NoSuchAnswerException;
import com.epam.lstrsum.exception.QuestionValidationException;
import com.epam.lstrsum.model.Answer;
import com.epam.lstrsum.model.Question;
import com.epam.lstrsum.model.QuestionWithAnswersCount;
import com.epam.lstrsum.model.User;
import com.epam.lstrsum.persistence.AnswerRepository;
import com.epam.lstrsum.persistence.QuestionRepository;
import com.epam.lstrsum.service.AnswerService;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;

@Service
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "answer")
@Slf4j
public class AnswerServiceImpl implements AnswerService {
    private static final int MIN_PAGE_SIZE = 0;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final MongoTemplate mongoTemplate;
    private final AnswerAggregator answerAggregator;
    @Setter
    private int searchDefaultPageSize;
    @Setter
    private int searchMaxPageSize;

    @Override
    @EmailNotification(template = NewAnswerNotificationTemplate.class)
    public AnswerAllFieldsDto addNewAnswer(AnswerPostDto answerPostDto, String email) {
        log.debug("Add new answer with email {}", email);
        return createAnswerAndGetAllFieldsDto(answerPostDto, email, getQuestionByIdOrThrowException(answerPostDto));
    }

    private Question getQuestionByIdOrThrowException(AnswerPostDto answerPostDto) {
        return Optional.ofNullable(questionRepository.findOne(answerPostDto.getQuestionId()))
                .orElseThrow(() -> new QuestionValidationException("No such question with id : " + answerPostDto.getQuestionId()));
    }

    private AnswerAllFieldsDto createAnswerAndGetAllFieldsDto(AnswerPostDto answerPostDto, String email, Question question) {
        Answer newAnswer = answerAggregator.answerPostDtoAndAuthorEmailToAnswer(question, answerPostDto, email);
        Answer saved = answerRepository.save(newAnswer);
        return answerAggregator.modelToAllFieldsDto(saved);
    }

    @Override
    @EmailNotification(template = NewAnswerNotificationTemplate.class)
    public AnswerAllFieldsDto addNewAnswerWithAllowedSub(AnswerPostDto answerPostDto, String email) {
        Question question = getQuestionByIdOrThrowException(answerPostDto);
        checkQuestionExistAndUserHasPermission(question, email);
        return createAnswerAndGetAllFieldsDto(answerPostDto, email, question);
    }

    private void checkQuestionExistAndUserHasPermission(Question question, String userEmail) {
        if (isNull(question) || !isUserAllowedSubOnQuestion(question, userEmail)) {
            throw new BusinessLogicException(
                    "Question isn't exist or user with email : '" + userEmail + " ' has no permission to question id : '" +
                            question.getQuestionId() +
                            "' and relative answers!");
        }
    }

    private boolean isUserAllowedSubOnQuestion(Question question, String userEmail) {
        return question.getAllowedSubs().stream().map(User::getEmail).filter(e -> e.equals(userEmail)).count() == 1;
    }

    @Override
    public void deleteAllAnswersOnQuestion(String questionId) {
        log.debug("Delete all answers on question with id {}", questionId);
        answerRepository.deleteAllByQuestionId_QuestionId(questionId);
    }

    @Override
    public List<QuestionWithAnswersCount> aggregateToCount(List<Question> questions) {
        final Aggregation aggregation = newAggregation(
                match(Criteria.where("questionId").in(questions)),
                group("questionId").count().as("count"),
                project("count").and("questionId").previousOperation()
        );

        final List<QuestionWithAnswersCount> mappedResults = mongoTemplate.aggregate(
                aggregation, Answer.class, QuestionWithAnswersCount.class
        ).getMappedResults();

        return completeNotFound(mappedResults, questions);
    }

    @Override
    public List<AnswerBaseDto> getAnswersByQuestionId(String questionId, int page, int size) {
        return answerRepository.findAnswerByQuestionId_QuestionIdOrderByCreatedAt(questionId, getPageable(page, size))
                .stream()
                .map(answerAggregator::modelToBaseDto)
                .collect(Collectors.toList());
    }

    private PageRequest getPageable(Integer page, Integer size) {
        if (size <= 0) {
            size = searchDefaultPageSize;
        } else if (size > searchMaxPageSize) {
            size = searchMaxPageSize;
        }

        if (page < MIN_PAGE_SIZE) {
            page = MIN_PAGE_SIZE;
        }
        return new PageRequest(page, size);
    }

    @Override
    public List<AnswerBaseDto> getAnswersByQuestionId(String questionId) {
        return getAnswersByQuestionId(questionId, 0, searchMaxPageSize);
    }

    @Override
    public Long getAnswerCountByQuestionId(String questionId) {
        return answerRepository.countAllByQuestionId(questionId);
    }

    private List<QuestionWithAnswersCount> completeNotFound(List<QuestionWithAnswersCount> resultsFromMongo, List<Question> sourceList) {
        final Map<String, QuestionWithAnswersCount> findQuestion = resultsFromMongo.stream()
                .collect(Collectors.toMap(
                        q -> q.getQuestionId().getQuestionId(),
                        Function.identity()
                ));

        return sourceList.stream()
                .map(q -> getDefaultCountIfNotFound(q, findQuestion))
                .collect(Collectors.toList());
    }

    private QuestionWithAnswersCount getDefaultCountIfNotFound(Question question, Map<String, QuestionWithAnswersCount> findQuestion) {
        val questionWithAnswersCount = findQuestion.get(question.getQuestionId());
        return nonNull(questionWithAnswersCount) ? questionWithAnswersCount : new QuestionWithAnswersCount(question, 0);
    }

    public Answer getAnswerById(String answerId) {
        return Optional.ofNullable(answerRepository.findOne(answerId))
                .orElseThrow(() -> new NoSuchAnswerException("No such Answer in user Collection"));
    }

    public void save(Answer answer) {
        log.debug("Saved answer with id {}", answer.getAnswerId());
        answerRepository.save(answer);
    }
}
