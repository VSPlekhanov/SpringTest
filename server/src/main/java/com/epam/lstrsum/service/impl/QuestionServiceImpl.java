package com.epam.lstrsum.service.impl;

import com.epam.lstrsum.aggregators.QuestionAggregator;
import com.epam.lstrsum.dto.question.*;
import com.epam.lstrsum.email.EmailNotification;
import com.epam.lstrsum.email.template.NewQuestionNotificationTemplate;
import com.epam.lstrsum.exception.NoSuchRequestException;
import com.epam.lstrsum.exception.QuestionValidationException;
import com.epam.lstrsum.model.Question;
import com.epam.lstrsum.model.User;
import com.epam.lstrsum.persistence.QuestionRepository;
import com.epam.lstrsum.service.QuestionService;
import com.epam.lstrsum.service.TagService;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;


@Service
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "question")
@Slf4j
public class QuestionServiceImpl implements QuestionService {

    private final static int QUESTION_TITLE_LENGTH = 5;
    private final static int QUESTION_TEXT_LENGTH = 5;
    private final static int MIN_PAGE_SIZE = 0;

    @Setter
    private int searchDefaultPageSize;

    @Setter
    private int searchMaxPageSize;

    private final TagService tagService;
    private final QuestionAggregator questionAggregator;
    private final QuestionRepository questionRepository;
    private final MongoTemplate mongoTemplate;

    @Override
    public List<QuestionAllFieldsDto> findAll() {
        List<Question> questionList = questionRepository.findAll();
        return mapList(questionList, questionAggregator::modelToAllFieldsDto);
    }

    @Override
    public List<QuestionAllFieldsDto> search(String searchQuery, Integer page, Integer size) {
        if (isNull(size) || size <= 0) {
            size = searchDefaultPageSize;
        }
        if (size > searchMaxPageSize) {
            size = searchMaxPageSize;
        }
        if (isNull(page) || page < MIN_PAGE_SIZE) {
            page = MIN_PAGE_SIZE;
        }

        Sort sort = new Sort("score");
        TextCriteria criteria = TextCriteria.forDefaultLanguage().matching(searchQuery);
        List<Question> questionList = questionRepository.findAllBy(criteria, new PageRequest(page, size, sort));

        return questionList
                .stream()
                .map(questionAggregator::modelToAllFieldsDto)
                .collect(Collectors.toList());
    }

    @Override
    public Long getTextSearchResultsCount(String query) {
        return questionRepository.getTextSearchResultsCount(query);
    }

    @Override
    public List<QuestionWithAnswersCountDto> findAllQuestionsBaseDto(int questionPage, int questionAmount) {
        Pageable pageable = new PageRequest(questionPage, questionAmount);
        List<Question> questionList = questionRepository.findAllByOrderByCreatedAtDesc(pageable);
        return mapList(questionList, questionAggregator::modelToAnswersCountDto);
    }

    @Override
    public List<String> getRelevantTags(String key) {
        return tagService.getTagsRating().stream().filter(e -> e.startsWith(key)).collect(Collectors.toList());
    }

    @Override
    @CacheEvict(value = "tagsRating", allEntries = true)
    @EmailNotification(template = NewQuestionNotificationTemplate.class)
    public Question addNewQuestion(QuestionPostDto questionPostDto, String email) {
        log.debug("Add new Question with email {}", email);
        validateQuestionData(questionPostDto, email);
        Question newQuestion = questionAggregator.questionPostDtoAndAuthorEmailToQuestion(questionPostDto, email);
        return questionRepository.save(newQuestion);
    }

    @Override
    public QuestionAllFieldsDto getQuestionAllFieldDtoByQuestionId(String questionId) {
        Question question = questionRepository.findOne(questionId);
        return questionAggregator.modelToAllFieldsDto(question);
    }

    @Override
    public QuestionAppearanceDto getQuestionAppearanceDotByQuestionId(String questionId) {
        Question question = questionRepository.findOne(questionId);
        return questionAggregator.modelToQuestionAppearanceDto(question);
    }

    @Override
    public Question getQuestionById(String questionId) {
        return questionRepository.findOne(questionId);
    }

    @Override
    public boolean contains(String objectsId) {
        return questionRepository.findOne(objectsId) != null;
    }

    @Override
    public Question findQuestionByTitleAndAuthorEmail(String title, User authorId) {
        return questionRepository.findQuestionByTitleAndAuthorId(title, authorId).
                orElseThrow(() -> new NoSuchRequestException("No such question"));
    }

    @Override
    public Long getQuestionCount() {
        return questionRepository.count();
    }

    @Override
    public void addAttachmentsToQuestion(String questionId, List<String> attachmentIds) {
        mongoTemplate.findAndModify(
                new Query(Criteria.where("questionId").is(questionId)),
                new Update().addToSet("attachmentIds").each(attachmentIds),
                Question.class
        );
    }

    private void validateQuestionData(QuestionPostDto questionPostDto, String email) {
        if (questionPostDto == null) {
            throw new QuestionValidationException("Post question should have json for QuestionPostDto");
        }
        if (email == null) {
            throw new QuestionValidationException("probably should`nt appear at all, problems with SSO");
        }
        if ((questionPostDto.getText() == null) || (questionPostDto.getTitle() == null)) {
            throw new QuestionValidationException("null fields found in question " + questionPostDto.toJson());
        }
        if (questionPostDto.getTitle().length() < QUESTION_TITLE_LENGTH) {
            throw new QuestionValidationException("Title is too short " + questionPostDto.toJson());
        }
        if (questionPostDto.getText().length() < QUESTION_TEXT_LENGTH) {
            throw new QuestionValidationException("Text is too short " + questionPostDto.toJson());
        }
    }

    private static <T1, T2> List<T2> mapList(List<T1> list, Function<T1, T2> mapper) {
        List<T2> result = new ArrayList<>();
        for (T1 value : list) {
            result.add(mapper.apply(value));
        }
        return result;
    }
}