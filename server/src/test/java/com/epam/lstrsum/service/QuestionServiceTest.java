package com.epam.lstrsum.service;

import com.epam.lstrsum.SetUpDataBaseCollections;
import com.epam.lstrsum.aggregators.QuestionAggregator;
import com.epam.lstrsum.dto.question.QuestionAllFieldsDto;
import com.epam.lstrsum.dto.question.QuestionAppearanceDto;
import com.epam.lstrsum.dto.question.QuestionPostDto;
import com.epam.lstrsum.dto.question.QuestionWithAnswersCountDto;
import com.epam.lstrsum.exception.NoSuchUserException;
import com.epam.lstrsum.exception.QuestionValidationException;
import com.epam.lstrsum.model.Question;
import com.epam.lstrsum.model.User;
import com.epam.lstrsum.persistence.QuestionRepository;
import com.epam.lstrsum.persistence.UserRepository;
import com.epam.lstrsum.testutils.InstantiateUtil;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.TextCriteria;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.epam.lstrsum.testutils.InstantiateUtil.*;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class QuestionServiceTest extends SetUpDataBaseCollections {
    private static final String SEARCH_PHRASE = "android";
    private static final String SEARCH_TAG = "j";
    private static final String MOST_POPULAR_TAG = "javascript";

    private static final int PAGE_SIZE = 1;
    private static final int START_PAGE = 0;
    private static final int NONEXISTENT_PAGE = 2;

    private static List<String> allowedSubsList = Arrays.asList("Bob_Hoplins@epam.com", "Tyler_Greeds@epam.com",
            "Donald_Gardner@epam.com", "Ernest_Hemingway@epam.com");

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
        List<QuestionAllFieldsDto> actualList = questionService.search(SEARCH_PHRASE, START_PAGE, 100_000);

        assertThatListHasRightSizeAndContainsCorrectValue(actualList, 2, SEARCH_PHRASE);
    }

    @Test
    public void searchWithTooBigPageSize() {
        List<QuestionAllFieldsDto> actualList = questionService.search(SEARCH_PHRASE, START_PAGE, 100_000);

        assertThatListHasRightSizeAndContainsCorrectValue(actualList, 2, SEARCH_PHRASE);
    }

    @Test
    public void searchWithNegativeStartPage() {
        List<QuestionAllFieldsDto> actualList = questionService.search(SEARCH_PHRASE, -1, PAGE_SIZE);

        assertThatListHasRightSizeAndContainsCorrectValue(actualList, 1, SEARCH_PHRASE);
    }

    @Test
    public void searchWithAllowedSubReturnsCorrectValueFromStartOfList() {
        List<QuestionAllFieldsDto> list =
                questionService.searchWithAllowedSub(SEARCH_PHRASE, START_PAGE, null, "Steven_Tyler@epam.com");
        List<QuestionAllFieldsDto> anotherList =
                questionService.searchWithAllowedSub(SEARCH_PHRASE, START_PAGE, null, EXISTING_USER_EMAIL);

        assertThatListHasRightSizeAndContainsCorrectValue(list, 1, SEARCH_PHRASE);
        assertThatListHasRightSizeAndContainsCorrectValue(anotherList, 2, SEARCH_PHRASE);
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

    @Test
    public void getTextSearchResultsCountWithAllowedSubCorrect() {
        final TextCriteria textCriteria =
                TextCriteria.forDefaultLanguage().matching(SEARCH_PHRASE);
        long expected = questionRepository.countAllByAllowedSubsContains(userRepository.findOne("5u"), textCriteria);
        Long actual = questionService.getTextSearchResultsCountWithAllowedSub(SEARCH_PHRASE, EXISTING_USER_EMAIL);
        long anotherExpected = questionRepository.countAllByAllowedSubsContains(userRepository.findOne("6u"), textCriteria);
        Long anotherActual = questionService.getTextSearchResultsCountWithAllowedSub(SEARCH_PHRASE, "Steven_Tyler@epam.com");

        assertThat(actual, is(expected));
        assertThat(actual).isEqualTo(2L);
        assertThat(anotherActual, is(anotherExpected));
        assertThat(anotherActual).isEqualTo(1L);
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
                "just some text", 1501145960439L, allowedSubsList, emptyList());
        String authorEmail = "John_Doe@epam.com";
        String newQuestionId = questionService.addNewQuestion(postDto, authorEmail).getQuestionId();
        assertThat(questionService.contains(newQuestionId), is(true));
    }

    @Test
    public void questionServiceIsAbleToGetQuestionAppearanceDTOFromDBIfIdIsValid() {
        Optional<QuestionAppearanceDto> dtoQuestionDto =
                questionService.getQuestionAppearanceDtoByQuestionId(InstantiateUtil.EXISTING_QUESTION_ID, someString());

        assertThat(dtoQuestionDto.isPresent(), is(true));
    }

    @Test
    public void getQuestionAppearanceDtoByQuestionIdIfIdIsNotExists() {
        Optional<QuestionAppearanceDto> dtoQuestionDto =
                questionService.getQuestionAppearanceDtoByQuestionId(InstantiateUtil.NON_EXISTING_QUESTION_ID, someString());

        assertThat(dtoQuestionDto.isPresent(), is(false));
    }

    @Test
    public void getQuestionAppearanceDtoByQuestionIdWhichIsSubscribedByUser() {
        Optional<QuestionAppearanceDto> questionDto = questionService.getQuestionAppearanceDtoByQuestionId(
                InstantiateUtil.EXISTING_QUESTION_ID,
                "Bob_Hoplins@epam.com");
        questionDto.ifPresent(
                questionAppearanceDto -> assertTrue(questionAppearanceDto.isCurrentUserSubscribed()));
    }

    @Test
    public void getQuestionAppearanceDtoByQuestionIdWhichIsNotSubscribedByUser() {
        Optional<QuestionAppearanceDto> questionDto = questionService.getQuestionAppearanceDtoByQuestionId(
                InstantiateUtil.EXISTING_QUESTION_ID,
                "John_Doe@epam.com");
        questionDto.ifPresent(
                questionAppearanceDto -> assertFalse(questionAppearanceDto.isCurrentUserSubscribed()));
    }

    @Test
    public void getQuestionAppearanceDtoByQuestionIdWithoutPermissions() {
        Optional<QuestionAppearanceDto> questionDto = questionService.getQuestionAppearanceDtoByQuestionIdWithAllowedSub(
                InstantiateUtil.EXISTING_QUESTION_ID,
                "Tyler_Derden@mylo.com");
        assertFalse(questionDto.isPresent());
    }

    @Test(expected = QuestionValidationException.class)
    public void questionServiceThrowsQuestionValidationExceptionForNullTitleInPostDto() {
        QuestionPostDto postDto = new QuestionPostDto(null, new String[]{"1", "2", "3", "go"},
                someString(), someLong(), allowedSubsList, emptyList());
        questionService.addNewQuestion(postDto, SOME_USER_EMAIL);
    }

    @Test(expected = QuestionValidationException.class)
    public void questionServiceThrowsQuestionValidationExceptionForNullTextInPostDtoTest() {
        QuestionPostDto postDto = new QuestionPostDto(someString(), new String[]{"1", "2", "3", "go"},
                null, someLong(), allowedSubsList, emptyList());
        questionService.addNewQuestion(postDto, SOME_USER_EMAIL);
    }

    @Test(expected = QuestionValidationException.class)
    public void questionServiceThrowsQuestionValidationExceptionIfTextIsTooShortInPostDtoTest() {
        QuestionPostDto postDto = new QuestionPostDto(someString(), new String[]{"1", "2", "3", "go"},
                "", someLong(), allowedSubsList, emptyList());
        questionService.addNewQuestion(postDto, SOME_USER_EMAIL);
    }

    @Test(expected = QuestionValidationException.class)
    public void questionServiceThrowsQuestionValidationExceptionIfTitleIsTooShortInPostDtoTest() {
        QuestionPostDto postDto = new QuestionPostDto("tle", new String[]{"1", "2", "3", "go"},
                someString(), someLong(), allowedSubsList, emptyList());
        questionService.addNewQuestion(postDto, SOME_USER_EMAIL);
    }

    @Test(expected = NoSuchUserException.class)
    public void questionServiceThrowsNoSuchUserExceptionIfAuthorEmailIsWrongTest() {
        QuestionPostDto postDto = someQuestionPostDtoWithAllowedSubs(allowedSubsList);
        questionService.addNewQuestion(postDto, someString());
    }

    @Test(expected = NoSuchUserException.class)
    public void questionServiceThrowsNoSuchUserExceptionIfAuthorEmailIsOutsideEpamTest() {
        QuestionPostDto postDto = someQuestionPostDtoWithAllowedSubs(allowedSubsList);
        String authorEmail = "user_outside_epam@gmail.com";
        questionService.addNewQuestion(postDto, authorEmail);
    }

    @Test(expected = NoSuchUserException.class)
    public void questionServiceThrowsNoSuchUserExceptionIfAllowedSubsWrongTest() {
        QuestionPostDto postDto = someQuestionPostDtoWithAllowedSubs(
                Arrays.asList("Bob_Hoplins@epam.com", "Tyler_Greeds@epam.com", "Donald_Gardner@epam.com", "no_such_user_in_epam@epam.com"));
        questionService.addNewQuestion(postDto, SOME_USER_EMAIL);
    }

    @Test(expected = NoSuchUserException.class)
    public void questionServiceThrowsNoSuchUserExceptionIfAllowedSubsOutsideEpamTest() {
        QuestionPostDto postDto = someQuestionPostDtoWithAllowedSubs(
                Arrays.asList("Bob_Hoplins@epam.com", "Tyler_Greeds@epam.com", "Donald_Gardner@epam.com", "user_outside_epam@gmail.com"));
        questionService.addNewQuestion(postDto, SOME_USER_EMAIL);
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
    }

    @Test
    public void deleteNotValidQuestion() {
        final String notValidQuestionId = NON_EXISTING_QUESTION_ID;

        questionService.delete(notValidQuestionId);

        assertThat(questionRepository.findOne(notValidQuestionId)).isNull();
    }

    @Test
    public void findAllQuestionBaseDtoWithAllowedSub() {
        assertThat(questionService.findAllQuestionBaseDtoWithAllowedSub(0, 100, EXISTING_USER_EMAIL))
                .hasSize(6);
    }

    @Test
    public void addQuestionWithSuccessInitializeSubscribersListTest() {
        QuestionPostDto questionPostDto = someQuestionPostDto();
        String authorEmail = "John_Doe@epam.com";
        questionPostDto.setAllowedSubs(Arrays.asList(authorEmail, "Bob_Hoplins@epam.com", "Donald_Gardner@epam.com"));

        List<Question> questionListBeforeUpdate = questionRepository.findAll();

        Question question = questionService.addNewQuestion(questionPostDto, authorEmail);
        List<User> expectedSubscribers = question.getAllowedSubs();

        assertThat(questionRepository.findAll().size()).isEqualTo(questionListBeforeUpdate.size() + 1);

        List<User> subscribers = question.getSubscribers();
        assertThat(subscribers).containsExactlyInAnyOrder(expectedSubscribers.toArray(new User[subscribers.size()]));
    }

}
