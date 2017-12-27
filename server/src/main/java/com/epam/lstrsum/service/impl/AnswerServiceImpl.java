package com.epam.lstrsum.service.impl;

import com.epam.lstrsum.aggregators.AnswerAggregator;
import com.epam.lstrsum.dto.answer.AnswerAllFieldsDto;
import com.epam.lstrsum.dto.answer.AnswerBaseDto;
import com.epam.lstrsum.dto.answer.AnswerPostDto;
import com.epam.lstrsum.email.EmailNotification;
import com.epam.lstrsum.email.template.NewAnswerNotificationTemplate;
import com.epam.lstrsum.exception.AnswersWithSameIdException;
import com.epam.lstrsum.exception.BusinessLogicException;
import com.epam.lstrsum.exception.NoSuchAnswerException;
import com.epam.lstrsum.exception.QuestionValidationException;
import com.epam.lstrsum.model.Answer;
import com.epam.lstrsum.model.Question;
import com.epam.lstrsum.model.QuestionWithAnswersCount;
import com.epam.lstrsum.model.User;
import com.epam.lstrsum.persistence.QuestionRepository;
import com.epam.lstrsum.persistence.UserRepository;
import com.epam.lstrsum.service.AnswerService;
import com.epam.lstrsum.utils.MessagesHelper;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
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

    @Autowired
    private final MessagesHelper messagesHelper;

    @Setter
    private int searchDefaultPageSize;
    @Setter
    private int searchMaxPageSize;

    @Override
    @EmailNotification(template = NewAnswerNotificationTemplate.class)
    public AnswerAllFieldsDto addNewAnswer(AnswerPostDto answerPostDto, String email) {
        log.debug("Add new answer with email {}", email);
        return createAnswerAndGetAllFieldsDto(answerPostDto, email);
    }

    @Override
    @EmailNotification(template = NewAnswerNotificationTemplate.class)
    public AnswerAllFieldsDto addNewAnswerWithAllowedSub(AnswerPostDto answerPostDto, String email) {
        Question question = getQuestionByIdOrThrowException(answerPostDto);
        checkQuestionExistAndUserHasPermission(question, email);
        return createAnswerAndGetAllFieldsDto(answerPostDto, email);
    }

    private Question getQuestionByIdOrThrowException(AnswerPostDto answerPostDto) {
        return Optional.ofNullable(questionRepository.findOne(answerPostDto.getQuestionId()))
                .orElseThrow(() -> new QuestionValidationException(MessageFormat.format(messagesHelper.get("validation.service.no-such-question-with-id"),
                                                                    answerPostDto.getQuestionId())));
    }

    private void checkQuestionExistAndUserHasPermission(Question question, String userEmail) {
        if (isNull(question) || !isUserAllowedSubOnQuestion(question, userEmail)) {
            throw new BusinessLogicException(
                    MessageFormat.format(messagesHelper.get("validation.service.question-not-exist-or-user-has-no-permission-to-question"),
                            userEmail, question.getQuestionId()));
        }
    }

    private boolean isUserAllowedSubOnQuestion(Question question, String userEmail) {
        return question.getAllowedSubs().stream().map(User::getEmail).map(String::toLowerCase).filter(e -> e.equals(userEmail)).count() == 1;
    }

    private AnswerAllFieldsDto createAnswerAndGetAllFieldsDto(AnswerPostDto answerPostDto, String email) {
        Answer newAnswer = answerAggregator.answerPostDtoAndAuthorEmailToAnswer(answerPostDto, email);
        Answer saved = addAnswerOnQuestion(newAnswer, answerPostDto.getQuestionId());
        return answerAggregator.modelToAllFieldsDto(saved);
    }

    @Override
    public List<QuestionWithAnswersCount> aggregateToCount(List<Question> questions) {
        return questions.stream().map(question ->
                new QuestionWithAnswersCount(question,
                        Optional.ofNullable(question.getAnswers()).orElse(Collections.emptyList()).size()))
                .collect(Collectors.toList());
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
                sort(Sort.Direction.DESC, "createdAt"),
                skip((long) size * page),
                limit(size)
        );

        return mongoTemplate.aggregate(aggregation, Question.class, Answer.class)
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
        Query query = new Query(Criteria.where("_id").is(questionId));
        List<Answer> answers = mongoTemplate.findOne(query, Question.class).getAnswers();

        return isNull(answers) ? 0L : (long) answers.size();
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

    @Override
    public Answer getAnswerByIdAndQuestionId(String answerId, String questionId) {
        Aggregation aggregation = newAggregation(
                // TODO: 01.11.17 String or ObjectId(String) - what's the correct way to do this all around the project?
                match(Criteria.where("_id").is(questionId)),
                project("answers").andExclude("_id"),
                unwind("answers"),
                replaceRoot("answers"),
                match(Criteria.where("answerId").is(answerId))
        );

        // TODO: 01.11.17 This doesn't work if we change Question.class to Question.QUESTION_COLLECTION_NAME
        List<Answer> answers = mongoTemplate.aggregate(aggregation, Question.class, Answer.class).getMappedResults();
        if (answers.isEmpty() || isNull(answers.get(0))) throw new NoSuchAnswerException(messagesHelper.get("validation.service.no-such-answer-in-this-question"));
        if (answers.size() > 1) throw new AnswersWithSameIdException(messagesHelper.get("validation.service.answers-with-same-id-found"));

        return answers.get(0);
    }

    public void save(Answer answer, String questionId) {
        log.debug("Saved answer with id {}", answer.getAnswerId());
        addAnswerOnQuestion(answer, questionId);
    }

    private Answer addAnswerOnQuestion(Answer answer, String questionId) {
        Query findQuestion = new Query(Criteria.where("_id").is(questionId));
        // TODO: 25.10.17 can addToSet's duplicates control be changed? probably not.
        // (see https://docs.mongodb.com/manual/reference/operator/update/addToSet/ )
        Update addAnswer = new Update().addToSet("answers", answer);

        mongoTemplate.findAndModify(findQuestion, addAnswer, Question.class);

        return getAnswerByIdAndQuestionId(answer.getAnswerId(), questionId);
    }
}
