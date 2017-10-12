package com.epam.lstrsum.service.impl;

import com.epam.lstrsum.aggregators.AttachmentAggregator;
import com.epam.lstrsum.aggregators.QuestionAggregator;
import com.epam.lstrsum.dto.attachment.AttachmentPropertiesDto;
import com.epam.lstrsum.dto.question.QuestionAllFieldsDto;
import com.epam.lstrsum.dto.question.QuestionAppearanceDto;
import com.epam.lstrsum.dto.question.QuestionPostDto;
import com.epam.lstrsum.dto.question.QuestionWithAnswersCountDto;
import com.epam.lstrsum.email.EmailNotification;
import com.epam.lstrsum.email.template.NewQuestionNotificationTemplate;
import com.epam.lstrsum.exception.NoSuchRequestException;
import com.epam.lstrsum.exception.QuestionValidationException;
import com.epam.lstrsum.model.Attachment;
import com.epam.lstrsum.model.Question;
import com.epam.lstrsum.model.QuestionWithAnswersCount;
import com.epam.lstrsum.model.Subscription;
import com.epam.lstrsum.model.User;
import com.epam.lstrsum.persistence.AttachmentRepository;
import com.epam.lstrsum.persistence.QuestionRepository;
import com.epam.lstrsum.service.AnswerService;
import com.epam.lstrsum.service.AttachmentService;
import com.epam.lstrsum.service.ElasticSearchService;
import com.epam.lstrsum.service.QuestionService;
import com.epam.lstrsum.service.TagService;
import com.epam.lstrsum.service.UserService;
import com.mongodb.DBRef;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
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
    private final AttachmentRepository attachmentRepository;
    private final AttachmentAggregator attachmentAggregator;
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
    public String smartSearch(String searchQuery, int page, int size) {
        return elasticSearchService.smartSearch(searchQuery, page, size);
    }

    @Override
    public Long getTextSearchResultsCount(String query) {
        return questionRepository.getTextSearchResultsCount(query);
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
        List<Question> questionList = questionRepository.findAllByAllowedSubsContainsOrderByCreatedAtDesc(
                userService.findUserByEmail(userEmail), pageable
        );
        final List<QuestionWithAnswersCount> questionWithAnswersCounts = answerService.aggregateToCount(questionList);

        return mapList(questionWithAnswersCounts, questionAggregator::modelToAnswersCountDto);
    }

    @Override
    public List<String> getRelevantTags(String key) {
        return tagService.getFilteredTagsRating(key);
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

    @SneakyThrows
    @Override
    @CacheEvict(value = "tagsRating", allEntries = true)
    @EmailNotification(template = NewQuestionNotificationTemplate.class)
    public Question addNewQuestion(QuestionPostDto questionPostDto, String email, MultipartFile[] files) {
        log.debug("Add new Question with email {}", email);
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
    public QuestionAllFieldsDto getQuestionAllFieldDtoByQuestionId(String questionId) {
        Question question = questionRepository.findOne(questionId);
        return questionAggregator.modelToAllFieldsDto(question);
    }

    @Override
    public Optional<QuestionAppearanceDto> getQuestionAppearanceDtoByQuestionId(String questionId) {
        Question question = questionRepository.findOne(questionId);
        if (isNull(question)) {
            return Optional.empty();
        }

        QuestionAppearanceDto questionAppearanceDto = questionAggregator.modelToQuestionAppearanceDto(question);

        List<String> attachmentIds = question.getAttachmentIds();
        if (nonNull(attachmentIds) && !attachmentIds.isEmpty()) {
            ArrayList<Attachment> attachments = (ArrayList<Attachment>) attachmentRepository.findAll(attachmentIds);
            List<AttachmentPropertiesDto> attachmentsDto = attachmentAggregator.modelToListPropertiesDto(attachments);
            questionAppearanceDto.setAttachments(attachmentsDto);
        } else {
            questionAppearanceDto.setAttachments(new ArrayList<AttachmentPropertiesDto>());
        }

        return Optional.ofNullable(questionAppearanceDto);
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
        return questionRepository.countAllByAllowedSubs(userService.findUserByEmail(userEmail));
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
        deleteSubscriptionsByQuestionId(id);
    }

    @Override
    public void deleteSubscriptionsByQuestionId(String questionId) {
        log.debug("Delete all subscriptions with questionId {}", questionId);

        final Query findAll = new Query();
        final Update pullQuestion = new Update().pull(
                "questionIds", new DBRef(Question.QUESTION_COLLECTION_NAME, questionId)
        );

        mongoTemplate.updateMulti(findAll, pullQuestion, Subscription.class);
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
}
