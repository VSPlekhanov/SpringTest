package com.epam.lstrsum.service.impl;

import com.epam.lstrsum.aggregators.QuestionAggregator;
import com.epam.lstrsum.dto.question.*;
import com.epam.lstrsum.email.EmailNotification;
import com.epam.lstrsum.email.template.NewQuestionNotificationTemplate;
import com.epam.lstrsum.exception.NoSuchRequestException;
import com.epam.lstrsum.exception.QuestionValidationException;
import com.epam.lstrsum.model.Question;
import com.epam.lstrsum.model.QuestionWithAnswersCount;
import com.epam.lstrsum.model.User;
import com.epam.lstrsum.persistence.QuestionRepository;
import com.epam.lstrsum.service.*;
import com.epam.lstrsum.utils.MessagesHelper;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.web.multipart.MultipartFile;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;


@Service
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "question")
@Slf4j
public class QuestionServiceImpl implements QuestionService {

    private final static int QUESTION_TITLE_LENGTH = 5;
    private final static int QUESTION_TEXT_LENGTH = 5;

    private final static int MIN_PAGE_SIZE = 0;

    private final TagService tagService;
    private final QuestionAggregator questionAggregator;
    private final QuestionRepository questionRepository;
    private final MongoTemplate mongoTemplate;
    private final ElasticSearchService elasticSearchService;
    private final AnswerService answerService;
    private final UserService userService;
    private final AttachmentService attachmentService;

    @Autowired
    private MessagesHelper messagesHelper;

    @Setter
    private int searchDefaultPageSize;

    @Setter
    private int searchMaxPageSize;

    private static <T1, T2> List<T2> mapList(List<T1> list, Function<T1, T2> mapper) {
        List<T2> result = new ArrayList<>();
        for (T1 value : list) {
            result.add(mapper.apply(value));
        }
        return result;
    }

    @Override
    public List<QuestionAllFieldsDto> findAll() {
        List<Question> questionList = questionRepository.findAll();
        return mapList(questionList, questionAggregator::modelToAllFieldsDto);
    }

    @Override
    public List<QuestionAllFieldsDto> search(String searchQuery, Integer page, Integer size) {
        List<Question> questionList =
                questionRepository.findAllBy(getTextCriteriaForFullTextSearch(searchQuery), getPageableForFullTextSearch(page, size));

        return questionList
                .stream()
                .map(questionAggregator::modelToAllFieldsDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<QuestionAllFieldsDto> searchWithAllowedSub(String searchQuery, Integer page, Integer size, String email)
    {
        Optional<User> user = userService.findUserByEmailIfExist(email);
        return user.map(u -> {
            List<Question> questionList = questionRepository.findAllByAllowedSubsContains(u,
                    getTextCriteriaForFullTextSearch(searchQuery),
                    getPageableForFullTextSearch(page, size));

            return questionList
                    .stream()
                    .map(questionAggregator::modelToAllFieldsDto)
                    .collect(Collectors.toList());
        }).orElse(Collections.emptyList());
    }

    private TextCriteria getTextCriteriaForFullTextSearch(String searchQuery) {
        return TextCriteria.forDefaultLanguage().matching(searchQuery);
    }

    private PageRequest getPageableForFullTextSearch(Integer page, Integer size) {
        if (isNull(size) || size <= 0) size = searchDefaultPageSize;
        if (size > searchMaxPageSize) size = searchMaxPageSize;
        if (isNull(page) || page < MIN_PAGE_SIZE) page = MIN_PAGE_SIZE;

        return new PageRequest(page, size, new Sort("score"));
    }

    @Override
    public String smartSearch(String searchQuery, int page, int size) {
        return elasticSearchService.smartSearch(searchQuery, page, size);
    }

    @Override
    public String advancedSearch(String searchQuery, List<String> metaTags, Integer page, Integer size) {
        return elasticSearchService.advancedSearch(searchQuery, metaTags, page, size);
    }

    @Override
    public QuestionAllFieldsListDto elasticSimpleSearch(String searchString, List<String> metaTags, Integer page, Integer size) {
        return elasticSearchService.elasticSimpleSearch(searchString, metaTags, page, size);
    }

    @Override
    public String smartSearchWithAllowedSub(String searchQuery, int page, int size, String email) {
        return elasticSearchService.smartSearchWithAllowedSub(searchQuery, page, size, email);
    }

    @Override
    public Long getTextSearchResultsCount(String query) {
        return questionRepository.getTextSearchResultsCount(query);
    }

    @Override
    public Long getTextSearchResultsCountWithAllowedSub(String query, String email) {
        Optional<User> user = userService.findUserByEmailIfExist(email);
        return user
                .map(u -> questionRepository.countAllByAllowedSubsContains(u, getTextCriteriaForFullTextSearch(query)))
                .orElse(0L);
    }

    @Override
    public List<QuestionWithAnswersCountDto> findAllQuestionsBaseDto(int questionPage, int questionAmount) {
        Pageable pageable = new PageRequest(questionPage, questionAmount);
        List<Question> questionList = questionRepository.findAllByOrderByCreatedAtDesc(pageable);
        final List<QuestionWithAnswersCount> questionWithAnswersCounts = answerService.aggregateToCount(questionList);

        return mapList(questionWithAnswersCounts, questionAggregator::modelToAnswersCountDto);
    }

    @Override
    public List<QuestionWithAnswersCountDto> findAllQuestionBaseDtoWithAllowedSub(int questionPage, int questionAmount, String userEmail) {
        Pageable pageable = new PageRequest(questionPage, questionAmount);
        Optional<User> user = userService.findUserByEmailIfExist(userEmail);
        return  user.map(u -> {
                    List<Question> questionList = questionRepository.findAllByAllowedSubsContainsOrderByCreatedAtDesc(u, pageable);
                    return answerService.aggregateToCount(questionList).stream()
                                                .map(questionAggregator::modelToAnswersCountDto)
                                                .collect(Collectors.toList());
                    })
                .orElse(Collections.emptyList());
    }

    @Override
    public List<String> getRelevantTags(String key) {
        return tagService.getFilteredTagsRating(key);
    }

    @Override
    @CacheEvict(value = "tagsRating", allEntries = true)
    @EmailNotification(template = NewQuestionNotificationTemplate.class)
    public Question addNewQuestion(QuestionPostDto questionPostDto, String email) {
        log.debug("Add new Question from portal with email {}", email);
        validateQuestionData(questionPostDto, email);
        Question newQuestion = questionAggregator.questionPostDtoAndAuthorEmailToQuestion(questionPostDto, email);
        return questionRepository.save(newQuestion);
    }

    @SneakyThrows
    @Override
    @CacheEvict(value = "tagsRating", allEntries = true)
    @EmailNotification(template = NewQuestionNotificationTemplate.class)
    public Question addNewQuestion(QuestionPostDto questionPostDto, String email, MultipartFile[] files) {
        log.debug("Add new Question from portal with email {}", email);
        validateQuestionData(questionPostDto, email);

        List<String> attachmentIds = new ArrayList<>();
        if (nonNull(files)) {
            for (MultipartFile file : files) {
                attachmentIds.add(attachmentService.saveMultipartFile(file));
            }
        }

        Question newQuestion =
                questionAggregator.questionPostDtoAndAuthorEmailAndAttachmentsToQuestion(questionPostDto, email, attachmentIds);
        return questionRepository.save(newQuestion);
    }

    @Override
    @CacheEvict(value = "tagsRating", allEntries = true)
    @EmailNotification(template = NewQuestionNotificationTemplate.class, fromPortal = false)
    public Question addNewQuestionFromEmail(QuestionPostDto questionPostDto, String email) {
        log.debug("Add new Question from mail with email {}", email);
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
    public Optional<QuestionAppearanceDto> getQuestionAppearanceDtoByQuestionId(String questionId, String email) {
        Question question = questionRepository.findOne(questionId);
        if (isNull(question)) return Optional.empty();

        return getQuestionAppearanceDtoByQuestion(question, email);
    }

    private Optional<QuestionAppearanceDto> getQuestionAppearanceDtoByQuestion(Question question, String email) {
        QuestionAppearanceDto questionAppearanceDto = questionAggregator.modelToQuestionAppearanceDto(question, email);
        return  Optional.ofNullable(questionAppearanceDto);
    }

    @Override
    public Optional<QuestionAppearanceDto> getQuestionAppearanceDtoByQuestionIdWithAllowedSub(String questionId, String userEmail) {
        Question question = questionRepository.findOne(questionId);
        return userHasPermissionToViewQuestion(question, userEmail) ? getQuestionAppearanceDtoByQuestion(question, userEmail) : Optional.empty();
    }

    private boolean userHasPermissionToViewQuestion(Question question, String userEmail) {
        return !isNull(question) && question.getAllowedSubs().stream().map(User::getEmail).anyMatch(userEmail::equalsIgnoreCase);
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
    public Long getQuestionCountWithAllowedSub(String userEmail) {
        Optional<User> user = userService.findUserByEmailIfExist(userEmail);
        return user.map(questionRepository::countAllByAllowedSubs).orElse(0L);
    }

    @Override
    public void addAttachmentsToQuestion(String questionId, List<String> attachmentIds) {
        mongoTemplate.findAndModify(
                new Query(Criteria.where("questionId").is(questionId)),
                new Update().addToSet("attachmentIds").each(attachmentIds),
                Question.class
        );
        log.debug("Add new attachments to question with id {}", questionId);
    }

    @Override
    @CacheEvict(value = "tagsRating", allEntries = true)
    public void delete(String id) {
        log.debug("Delete question with id {}", id);
        questionRepository.delete(id);
    }


    private void validateQuestionData(QuestionPostDto questionPostDto, String email) {
        if (questionPostDto == null) {
            throw new QuestionValidationException(messagesHelper.get("validation.service.no-json-for-questionpostdto"));
        }
        if (email == null) {
            throw new QuestionValidationException(messagesHelper.get("validation.service.sso-problems"));
        }
        if ((questionPostDto.getText() == null) || (questionPostDto.getTitle() == null)) {
            throw new QuestionValidationException(MessageFormat.format(messagesHelper.get("validation.service.null-fields-in-question"),
                    questionPostDto.toJson()));
        }
        if (questionPostDto.getTitle().length() < QUESTION_TITLE_LENGTH) {
            throw new QuestionValidationException(MessageFormat.format(messagesHelper.get("validation.service.short-title"),
                    questionPostDto.toJson()));
        }
        if (questionPostDto.getText().length() < QUESTION_TEXT_LENGTH) {
            throw new QuestionValidationException(MessageFormat.format(messagesHelper.get("validation.service.short-text"),
                    questionPostDto.toJson()));
        }
    }
}
