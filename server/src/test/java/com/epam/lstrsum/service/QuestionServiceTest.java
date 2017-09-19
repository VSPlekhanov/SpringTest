package com.epam.lstrsum.service;

import com.epam.lstrsum.SetUpDataBaseCollections;
import com.epam.lstrsum.aggregators.QuestionAggregator;
import com.epam.lstrsum.dto.question.QuestionAllFieldsDto;
import com.epam.lstrsum.dto.question.QuestionAppearanceDto;
import com.epam.lstrsum.dto.question.QuestionPostDto;
import com.epam.lstrsum.dto.question.QuestionWithAnswersCountDto;
import com.epam.lstrsum.exception.QuestionValidationException;
import com.epam.lstrsum.model.Question;
import com.epam.lstrsum.model.Subscription;
import com.epam.lstrsum.model.User;
import com.epam.lstrsum.persistence.QuestionRepository;
import com.epam.lstrsum.persistence.UserRepository;
import com.epam.lstrsum.testutils.InstantiateUtil;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.epam.lstrsum.testutils.InstantiateUtil.EXISTING_QUESTION_ID;
import static com.epam.lstrsum.testutils.InstantiateUtil.NON_EXISTING_QUESTION_ID;
import static com.epam.lstrsum.testutils.InstantiateUtil.SOME_USER_EMAIL;
import static com.epam.lstrsum.testutils.InstantiateUtil.someInt;
import static com.epam.lstrsum.testutils.InstantiateUtil.someLong;
import static com.epam.lstrsum.testutils.InstantiateUtil.someQuestionPostDto;
import static com.epam.lstrsum.testutils.InstantiateUtil.someString;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;

public class QuestionServiceTest extends SetUpDataBaseCollections {
    private static final String SEARCH_PHRASE = "android";
    private static final String SEARCH_TAG = "j";
    private static final String MOST_POPULAR_TAG = "javascript";

    private static final int PAGE_SIZE = 1;
    private static final int START_PAGE = 0;
    private static final int NONEXISTENT_PAGE = 2;

    @Autowired
    private QuestionAggregator questionAggregator;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AnswerService answerService;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Test
    public void countQuestionCorrect() {
        assertThat(questionService.getQuestionCount())
                .isEqualTo(questionRepository.count())
                .isEqualTo(6);
    }

    @Test
    public void findAllReturnsCorrectValuesTest() {
        List<Question> questionList = questionRepository.findAll();
        List<QuestionAllFieldsDto> expectedAllFieldsDto = new ArrayList<>();
        for (Question question : questionList) {
            expectedAllFieldsDto.add(questionAggregator.modelToAllFieldsDto(question));
        }
        List<QuestionAllFieldsDto> actualList = questionService.findAll();

        assertEquals(expectedAllFieldsDto, actualList);
    }

    @Test
    public void searchReturnsEmptyListFromNonexistentPage() {
        Assertions.assertThat(questionService.search(SEARCH_PHRASE, NONEXISTENT_PAGE, PAGE_SIZE))
                .hasSize(0);
    }

    @Test
    public void searchReturnsCorrectValueFromStartOfList() {
        List<QuestionAllFieldsDto> actualList = questionService.search(SEARCH_PHRASE, START_PAGE, PAGE_SIZE);

        assertThatListHasRightSizeAndContainsCorrectValue(actualList, 1, SEARCH_PHRASE);
    }

    @Test
    public void searchWithNullSize() {
        List<QuestionAllFieldsDto> actualList = questionService.search(SEARCH_PHRASE, START_PAGE, null);

        assertThatListHasRightSizeAndContainsCorrectValue(actualList, 2, SEARCH_PHRASE);
    }

    @Test
    public void searchWithNegativeSize() {
        List<QuestionAllFieldsDto> actualList = questionService.search(SEARCH_PHRASE, START_PAGE, -5);

        assertThatListHasRightSizeAndContainsCorrectValue(actualList, 2, SEARCH_PHRASE);
    }

    @Test
    public void searchWithTooBigPageSize() {
        List<QuestionAllFieldsDto> actualList = questionService.search(SEARCH_PHRASE, START_PAGE, 100000);

        assertThatListHasRightSizeAndContainsCorrectValue(actualList, 2, SEARCH_PHRASE);
    }

    @Test
    public void searchWithNegativeStartPage() {
        List<QuestionAllFieldsDto> actualList = questionService.search(SEARCH_PHRASE, -1, PAGE_SIZE);

        assertThatListHasRightSizeAndContainsCorrectValue(actualList, 1, SEARCH_PHRASE);
    }

    @Test
    public void smartSearch() {
        assertThat(questionService.smartSearch("one", someInt(), someInt())).isNotNull();
    }

    @Test
    public void getTextSearchResultsCountCorrect() {
        long expected = questionRepository.getTextSearchResultsCount(SEARCH_PHRASE);
        Long actual = questionService.getTextSearchResultsCount(SEARCH_PHRASE);

        assertThat(actual, is(expected));
    }

    private void assertThatListHasRightSizeAndContainsCorrectValue(
            List<QuestionAllFieldsDto> actualList,
            int size,
            String searchPhrase
    ) {
        Assertions.assertThat(actualList)
                .hasSize(size);
        assertThat(actualList.isEmpty(), is(false));
        Assertions.assertThat(actualList)
                .allMatch(questionAllFieldsDto -> questionAllFieldsDto.getText().contains(searchPhrase));
    }

    @Test
    public void getQuestionReturnsCorrectDtoObject() {
        Question question = questionRepository.findOne(EXISTING_QUESTION_ID);
        QuestionAllFieldsDto dtoQuestionFromRepo = questionAggregator.modelToAllFieldsDto(question);
        QuestionAllFieldsDto dtoQuestionFromService = questionService.getQuestionAllFieldDtoByQuestionId(EXISTING_QUESTION_ID);
        assertThat(dtoQuestionFromRepo, is(equalTo(dtoQuestionFromService)));
    }

    @Test
    public void findAllQuestionsBaseDtoReturnsCorrectValuesTest() {
        int questionCount = (int) questionRepository.count();
        List<Question> questionList = questionRepository.findAllByOrderByCreatedAtDesc();

        final List<QuestionWithAnswersCountDto> expectedDtoList = answerService.aggregateToCount(questionList).stream()
                .map(questionAggregator::modelToAnswersCountDto)
                .collect(Collectors.toList());

        List<QuestionWithAnswersCountDto> actualList = questionService.findAllQuestionsBaseDto(0, questionCount);
        assertThat(actualList.equals(expectedDtoList), is(true));
    }

    @Test
    public void addNewQuestionConvertsPostDtoToQuestionAndPutsItIntoDbTest() {
        QuestionPostDto postDto = new QuestionPostDto("this the end", new String[]{"1", "2", "3", "go"},
                "just some text", 1501145960439L,
                Arrays.asList("Bob_Hoplins@epam.com", "Tyler_Greeds@epam.com",
                        "Donald_Gardner@epam.com", "Ernest_Hemingway@epam.com"), emptyList());
        String authorEmail = "John_Doe@epam.com";
        String newQuestionId = questionService.addNewQuestion(postDto, authorEmail).getQuestionId();
        assertThat(questionService.contains(newQuestionId), is(true));
    }

    @Test
    public void questionServiceIsAbleToGetQuestionAppearanceDTOFromDBIfIdIsValid() {
        Optional<QuestionAppearanceDto> dtoQuestionDto =
                questionService.getQuestionAppearanceDtoByQuestionId(InstantiateUtil.EXISTING_QUESTION_ID);

        assertThat(dtoQuestionDto.isPresent(), is(true));
    }

    @Test
    public void getQuestionAppearanceDtoByQuestionIdIfIdIsNotExists() {
        Optional<QuestionAppearanceDto> dtoQuestionDto =
                questionService.getQuestionAppearanceDtoByQuestionId(InstantiateUtil.NON_EXISTING_QUESTION_ID);

        assertThat(dtoQuestionDto.isPresent(), is(false));
    }

    @Test(expected = QuestionValidationException.class)
    public void questionServiceThrowsQuestionValidationExceptionForNullTitleInPostDto() {
        QuestionPostDto postDto = new QuestionPostDto(null, new String[]{"1", "2", "3", "go"},
                "just some text", 1501112360439L,
                Arrays.asList("Bob_Hoplins@epam.com", "Tyler_Greeds@epam.com",
                        "Donald_Gardner@epam.com", "Ernest_Hemingway@epam.com"), emptyList());
        String authorEmail = "John_Doe@epam.com";
        questionService.addNewQuestion(postDto, authorEmail);
    }

    @Test(expected = QuestionValidationException.class)
    public void questionServiceThrowsQuestionValidationExceptionForNullTextInPostDtoTest() {
        QuestionPostDto postDto = new QuestionPostDto(someString(), new String[]{"1", "2", "3", "go"},
                null, 1501145111439L,
                Arrays.asList("Bob_Hoplins@epam.com", "Tyler_Greeds@epam.com",
                        "Donald_Gardner@epam.com", "Ernest_Hemingway@epam.com"), emptyList());
        String authorEmail = "John_Doe@epam.com";
        questionService.addNewQuestion(postDto, authorEmail);
    }

    @Test(expected = QuestionValidationException.class)
    public void questionServiceThrowsQuestionValidationExceptionIfTextIsTooShortInPostDtoTest() {
        QuestionPostDto postDto = new QuestionPostDto(someString(), new String[]{"1", "2", "3", "go"},
                "", 1501145922239L,
                Arrays.asList("Bob_Hoplins@epam.com", "Tyler_Greeds@epam.com",
                        "Donald_Gardner@epam.com", "Ernest_Hemingway@epam.com"), emptyList());
        String authorEmail = "John_Doe@epam.com";
        questionService.addNewQuestion(postDto, authorEmail);
    }

    @Test(expected = QuestionValidationException.class)
    public void questionServiceThrowsQuestionValidationExceptionIfTitleIsTooShortInPostDtoTest() {
        QuestionPostDto postDto = new QuestionPostDto("tle", new String[]{"1", "2", "3", "go"},
                "just some text", 1501143330439L,
                Arrays.asList("Bob_Hoplins@epam.com", "Tyler_Greeds@epam.com",
                        "Donald_Gardner@epam.com", "Ernest_Hemingway@epam.com"), emptyList());
        String authorEmail = "John_Doe@epam.com";
        questionService.addNewQuestion(postDto, authorEmail);
    }

    @Test(expected = QuestionValidationException.class)
    public void addNewQuestionThrowsQuestionValidationExceptionIfQuestionPostDtoIsNullTest() {
        String authorEmail = someString();
        questionService.addNewQuestion(null, authorEmail);
    }

    @Test(expected = QuestionValidationException.class)
    public void addNewQuestionThrowsQuestionValidationExceptionIfQuestionAuthorEmailIsNullTest() {
        questionService.addNewQuestion(someQuestionPostDto(), null);
    }

    @Test
    public void getAvailableTagsReturnsExpectedValue() {
        List<String> actualTags = questionService.getRelevantTags(SEARCH_TAG);
        final int expectedSize = 3;

        assertThat(actualTags.size(), is(expectedSize));
        assertThat(actualTags.get(0), is(MOST_POPULAR_TAG));
    }

    @Test(expected = DuplicateKeyException.class)
    public void addTwoQuestionWithSimilarTitleFromOneAuthorRefused() {
        final QuestionPostDto questionPostDto = new QuestionPostDto(
                someString(), new String[]{}, someString(),
                someLong(), singletonList(SOME_USER_EMAIL), emptyList()
        );

        questionService.addNewQuestion(questionPostDto, SOME_USER_EMAIL);
        questionService.addNewQuestion(questionPostDto, SOME_USER_EMAIL);
    }

    @Test
    public void findQuestionByTitleAndAuthorId() {
        final String title = someString();
        final User user = userRepository.findOne("1u");
        final QuestionPostDto questionPostDto = new QuestionPostDto(
                title, new String[]{}, someString(),
                someLong(), singletonList(SOME_USER_EMAIL), emptyList()
        );

        questionService.addNewQuestion(questionPostDto, SOME_USER_EMAIL);

        assertThat(questionService.findQuestionByTitleAndAuthorEmail(title, user))
                .satisfies(q -> {
                    assertThat(q.getAuthorId().getEmail()).isEqualTo(SOME_USER_EMAIL);
                    assertThat(q.getTitle()).isEqualTo(title);
                });
    }

    @Test
    public void deleteQuestion() {
        final String validQuestionId = EXISTING_QUESTION_ID;
        assertThat(questionRepository.findOne(validQuestionId)).isNotNull();

        questionService.delete(validQuestionId);

        assertThat(questionRepository.findOne(validQuestionId)).isNull();

        assertThat(mongoTemplate.findAll(Subscription.class))
                .allSatisfy(
                        subscription -> assertThat(subscription.getQuestionIds().stream().map(Question::getQuestionId))
                                .doesNotContain(validQuestionId)
                );
    }

    @Test
    public void checkDeleting() {
        final String someQuestionWhoSubscribed = "6u_6r";

        assertThat(mongoTemplate.findAll(Subscription.class))
                .anySatisfy(
                        subscription -> assertThat(subscription.getQuestionIds().stream().map(Question::getQuestionId))
                                .contains(someQuestionWhoSubscribed)
                );

        questionService.deleteSubscriptionsByQuestionId(someQuestionWhoSubscribed);

        assertThat(mongoTemplate.findAll(Subscription.class))
                .allSatisfy(
                        subscription -> assertThat(subscription.getQuestionIds().stream().map(Question::getQuestionId))
                                .doesNotContain(someQuestionWhoSubscribed)
                );
    }

    @Test
    public void notUpdateAnything() {
        final String notExistingQuestionId = NON_EXISTING_QUESTION_ID;

        assertThat(mongoTemplate.findAll(Subscription.class))
                .allSatisfy(
                        subscription -> assertThat(subscription.getQuestionIds().stream().map(Question::getQuestionId))
                                .doesNotContain(notExistingQuestionId)
                );

        questionService.deleteSubscriptionsByQuestionId(notExistingQuestionId);
    }

    @Test
    public void deleteNotValidQuestion() {
        final String notValidQuestionId = NON_EXISTING_QUESTION_ID;

        questionService.delete(notValidQuestionId);

        assertThat(questionRepository.findOne(notValidQuestionId)).isNull();
    }
}
