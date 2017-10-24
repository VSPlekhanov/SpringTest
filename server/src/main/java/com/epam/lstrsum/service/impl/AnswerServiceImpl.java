package com.epam.lstrsum.service.impl;

import com.epam.lstrsum.aggregators.AnswerAggregator;
import com.epam.lstrsum.dto.answer.AnswerAllFieldsDto;
import com.epam.lstrsum.dto.answer.AnswerBaseDto;
import com.epam.lstrsum.dto.answer.AnswerPostDto;
import com.epam.lstrsum.email.EmailNotification;
import com.epam.lstrsum.email.template.NewAnswerNotificationTemplate;
import com.epam.lstrsum.exception.AnswerValidationException;
import com.epam.lstrsum.exception.NoSuchAnswerException;
import com.epam.lstrsum.model.Answer;
import com.epam.lstrsum.model.Question;
import com.epam.lstrsum.model.QuestionWithAnswersCount;
import com.epam.lstrsum.persistence.QuestionRepository;
import com.epam.lstrsum.persistence.UserRepository;
import com.epam.lstrsum.service.AnswerService;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.limit;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.replaceRoot;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.skip;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.sort;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.unwind;

@Service
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "answer")
@Slf4j
public class AnswerServiceImpl implements AnswerService {
    private static final int MIN_PAGE_SIZE = 0;
    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;
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
        validateAnswerData(answerPostDto, email);
        Answer newAnswer = answerAggregator.answerPostDtoAndAuthorEmailToAnswer(answerPostDto, email);
        Answer saved = addAnswerOnQuestion(newAnswer, answerPostDto.getQuestionId());
        return answerAggregator.modelToAllFieldsDto(saved);
    }

    @Override
    public List<QuestionWithAnswersCount> aggregateToCount(List<Question> questions) {
        List<QuestionWithAnswersCount> result = new ArrayList<QuestionWithAnswersCount>();

        for(Question question: questions){
            result.add(new QuestionWithAnswersCount(question, question.getAnswers().size()));
        }
        return result;
    }

    @Override
    public List<AnswerBaseDto> getAnswersByQuestionId(String questionId, int page, int size) {
        if (size <= 0) {
            size = searchDefaultPageSize;
        } else if (size > searchMaxPageSize) {
            size = searchMaxPageSize;
        }

        if (page < MIN_PAGE_SIZE) {
            page = MIN_PAGE_SIZE;
        }

        Aggregation aggregation = newAggregation(
                match(Criteria.where("_id").is(questionId)),
                project("answers").andExclude("_id"),
                unwind("answers"),
                replaceRoot("answers"),
                sort(Sort.Direction.ASC, "createdAt"),
                skip((long) size * page),
                limit(size)
        );

        return mongoTemplate.aggregate(aggregation, Question.QUESTION_COLLECTION_NAME, Answer.class)
                .getMappedResults().stream()
                .map(answerAggregator::modelToBaseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<AnswerBaseDto> getAnswersByQuestionId(String questionId) {
        return getAnswersByQuestionId(questionId, 0, searchMaxPageSize);
    }

    @Override
    public Long getAnswerCountByQuestionId(String questionId) {
        Query query = new Query(Criteria.where("questionId").is(questionId));
        return (long) mongoTemplate.findOne(query, Question.class).getAnswers().size();
    }

    private List<QuestionWithAnswersCount> completeNotFound(
            List<QuestionWithAnswersCount> resultsFromMongo, List<Question> sourceList
    ) {

        final Map<String, QuestionWithAnswersCount> findQuestion = resultsFromMongo.stream()
                .collect(Collectors.toMap(
                        q -> q.getQuestionId().getQuestionId(),
                        Function.identity()
                ));

        return sourceList.stream()
                .map(q -> getDefaultCountIfNotFound(q, findQuestion))
                .collect(Collectors.toList());
    }

    private QuestionWithAnswersCount getDefaultCountIfNotFound(
            Question question, Map<String, QuestionWithAnswersCount> findQuestion
    ) {
        final QuestionWithAnswersCount questionWithAnswersCount = findQuestion.get(question.getQuestionId());

        if (nonNull(questionWithAnswersCount)) {
            return questionWithAnswersCount;
        } else {
            return new QuestionWithAnswersCount(question, 0);
        }
    }

    private void validateAnswerData(AnswerPostDto answerPostDto, String email) {
        if (isNull(answerPostDto)) {
            throw new AnswerValidationException("Answer must be not null!");
        }
        if (isNull(email) || email.trim().isEmpty()) {
            throw new AnswerValidationException("Author must be not null or empty!");
        } else if (!userRepository.findByEmailIgnoreCase(email).isPresent()) {
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


    @Override
    public Answer getAnswerByIdAndQuestionId(String answerId, String questionId) {
        Aggregation aggregation = newAggregation(
                match(Criteria.where("_id").is(questionId)),
                project("answers").andExclude("_id"),
                unwind("answers"),
                replaceRoot("answers"),
                match(Criteria.where("_id").is(answerId))
        );

        Answer answer = mongoTemplate.aggregate(aggregation, Question.QUESTION_COLLECTION_NAME, Answer.class)
                .getUniqueMappedResult();
        if (isNull(answer)) throw new NoSuchAnswerException("No such Answer in this Question");

        return answer;
    }

    public void save(Answer answer, String questionId) {
        log.debug("Saved answer with id {}", answer.getAnswerId());
        addAnswerOnQuestion(answer, questionId);
    }

    private Answer addAnswerOnQuestion(Answer answer, String questionId) {
        Query findQuestion = new Query(Criteria.where("questionId").is(questionId));
        Update addAnswer = new Update().addToSet("answers", answer);

        mongoTemplate.findAndModify(findQuestion, addAnswer, Question.class);

        return getAnswerByIdAndQuestionId(answer.getAnswerId(), questionId);
    }
}
