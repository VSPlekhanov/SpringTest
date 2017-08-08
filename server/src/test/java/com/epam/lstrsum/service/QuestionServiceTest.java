package com.epam.lstrsum.service;

import com.epam.lstrsum.SetUpDataBaseCollections;
import com.epam.lstrsum.aggregators.QuestionAggregator;
import com.epam.lstrsum.dto.answer.AnswerBaseDto;
import com.epam.lstrsum.dto.question.QuestionAllFieldsDto;
import com.epam.lstrsum.dto.question.QuestionAppearanceDto;
import com.epam.lstrsum.dto.question.QuestionBaseDto;
import com.epam.lstrsum.dto.question.QuestionPostDto;
import com.epam.lstrsum.dto.user.UserBaseDto;
import com.epam.lstrsum.exception.QuestionValidationException;
import com.epam.lstrsum.model.Question;
import com.epam.lstrsum.persistence.QuestionRepository;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Objects.isNull;
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
    private static final QuestionAllFieldsDto QUESTION_WITH_ANDROID_TEXT = new QuestionAllFieldsDto(
            "1u_1r",
            "JsonMappingException on android spring httprequest",
            new String[]{"java", "android", "json", "spring"},
            LocalDateTime.parse("2016-04-19T11:00:00").toInstant(ZoneOffset.UTC),
            LocalDateTime.parse("2016-05-19T11:00:00").toInstant(ZoneOffset.UTC),
            new UserBaseDto(
                    "5u", "Ernest", "Hemingway", "Ernest_Hemingway@epam.com"
            ),
            0,
            Arrays.asList(
                    new UserBaseDto("2u", "Bob", "Hoplins", "Bob_Hoplins@epam.com"),
                    new UserBaseDto("3u", "Tyler", "Greeds", "Tyler_Greeds@epam.com"),
                    new UserBaseDto("4u", "Donald", "Gardner", "Donald_Gardner@epam.com"),
                    new UserBaseDto("5u", "Ernest", "Hemingway", "Ernest_Hemingway@epam.com"),
                    new UserBaseDto("6u", "Steven", "Tyler", "Steven_Tyler@epam.com")
            ),
            "I have this call in async task. All parameters are correct. In postman or advance rest client the call work fine and It return a json with a list of objects. But if I try to do this call in android with spring I have this error:"
    );

    @Autowired
    private QuestionAggregator questionAggregator;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private QuestionRepository questionRepository;

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

        assertThatListHasRightSizeAndContainsCorrectValue(actualList, 1, QUESTION_WITH_ANDROID_TEXT, SEARCH_PHRASE);
    }

    @Test
    public void searchWithNullSize() {
        List<QuestionAllFieldsDto> actualList = questionService.search(SEARCH_PHRASE, START_PAGE, null);

        assertThatListHasRightSizeAndContainsCorrectValue(actualList, 2, QUESTION_WITH_ANDROID_TEXT, SEARCH_PHRASE);
    }

    @Test
    public void searchWithNegativeSize() {
        List<QuestionAllFieldsDto> actualList = questionService.search(SEARCH_PHRASE, START_PAGE, -5);

        assertThatListHasRightSizeAndContainsCorrectValue(actualList, 2, QUESTION_WITH_ANDROID_TEXT, SEARCH_PHRASE);
    }

    @Test
    public void searchWithTooBigPageSize() {
        List<QuestionAllFieldsDto> actualList = questionService.search(SEARCH_PHRASE, START_PAGE, 100000);

        assertThatListHasRightSizeAndContainsCorrectValue(actualList, 2, QUESTION_WITH_ANDROID_TEXT, SEARCH_PHRASE);
    }

    @Test
    public void searchWithNegativeStartPage() {
        List<QuestionAllFieldsDto> actualList = questionService.search(SEARCH_PHRASE, -1, PAGE_SIZE);

        assertThatListHasRightSizeAndContainsCorrectValue(actualList, 1, QUESTION_WITH_ANDROID_TEXT, SEARCH_PHRASE);
    }

    @Test
    public void getTextSearchResultsCountCorrect() {
        int expected = questionRepository.getTextSearchResultsCount("android");
        Integer actual = questionService.getTextSearchResultsCount("android");

        assertThat(actual, is(expected));
    }

    private void assertThatListHasRightSizeAndContainsCorrectValue(
            List<QuestionAllFieldsDto> actualList,
            int size,
            QuestionAllFieldsDto oneOfQuestions,
            String searchPhrase
    ) {
        Assertions.assertThat(actualList)
                .hasSize(size)
                .contains(oneOfQuestions);
        assertThat(actualList.isEmpty(), is(false));
        Assertions.assertThat(actualList)
                .allMatch(questionAllFieldsDto -> questionAllFieldsDto.getText().contains(searchPhrase));
    }

    @Test
    public void getQuestionReturnsCorrectDtoObject() {
        Question question = questionRepository.findOne("1u_1r");
        QuestionAllFieldsDto dtoQuestionFromRepo = questionAggregator.modelToAllFieldsDto(question);
        QuestionAllFieldsDto dtoQuestionFromService = questionService.getQuestionAllFieldDtoByQuestionId("1u_1r");
        assertThat(dtoQuestionFromRepo, is(equalTo(dtoQuestionFromService)));
    }

    @Test
    public void findAllQuestionsBaseDtoReturnsCorrectValuesTest() {
        int questionCount = (int) questionRepository.count();
        List<Question> questionList = questionRepository.findAllByOrderByCreatedAtDesc();
        List<QuestionBaseDto> expectedDtoList = new ArrayList<>();
        for (Question question : questionList) {
            expectedDtoList.add(questionAggregator.modelToBaseDto(question));
        }
        List<QuestionBaseDto> actualList = questionService.findAllQuestionsBaseDto(0, questionCount);
        assertThat(actualList.equals(expectedDtoList), is(true));
    }

    @Test
    public void addNewQuestionConvertsPostDtoToQuestionAndPutsItIntoDbTest() {
        QuestionPostDto postDto = new QuestionPostDto("this the end", new String[]{"1", "2", "3", "go"},
                "just some text", 1501145960439L,
                Arrays.asList("Bob_Hoplins@epam.com", "Tyler_Greeds@epam.com",
                        "Donald_Gardner@epam.com", "Ernest_Hemingway@epam.com"));
        String authorEmail = "John_Doe@epam.com";
        String newQuestionId = questionService.addNewQuestion(postDto, authorEmail).getQuestionId();
        assertThat(questionService.contains(newQuestionId), is(true));
    }

    @Test
    public void questionServiceIsAbleToGetQuestionAppearanceDTOFromDBIfIdIsValid() {
        QuestionAppearanceDto dtoQuestionDto = questionService.getQuestionAppearanceDotByQuestionId("1u_1r");

        assertThat(isNull(dtoQuestionDto), is(false));
    }

    @Test
    public void questionServiceIsAbleToGetQuestionWithAnswersFromDBIfQuestionHasThem() {
        QuestionAppearanceDto dtoQuestionDtoWithAnswers = questionService.getQuestionAppearanceDotByQuestionId("1u_1r");

        assertThat(dtoQuestionDtoWithAnswers.getAnswers().isEmpty(), is(false));
    }

    @Test
    public void questionServiceIsAbleToGetQuestionWithoutAnswersFromDB() {
        QuestionAppearanceDto dtoQuestionDtoWithoutAnswers = questionService.getQuestionAppearanceDotByQuestionId("6u_6r");

        assertThat(dtoQuestionDtoWithoutAnswers.getAnswers().isEmpty(), is(true));
        assertThat(isNull(dtoQuestionDtoWithoutAnswers.getAnswers()), is(false));
    }

    @Test
    public void questionServiceReturnsListOfQuestionAnswersInCorrectAscOrder() {
        QuestionAppearanceDto dtoQuestionDtoWithAnswers = questionService.getQuestionAppearanceDotByQuestionId("1u_1r");
        List<AnswerBaseDto> questionAnswers = dtoQuestionDtoWithAnswers.getAnswers();

        for (int i = 1; i < questionAnswers.size(); i++) {
            assertThat(questionAnswers.get(i - 1).getCreatedAt().
                    isBefore(questionAnswers.get(i).getCreatedAt()), is(true));
        }
    }

    @Test(expected = QuestionValidationException.class)
    public void questionServiceThrowsQuestionValidationExceptionForNullTitleInPostDto() {
        QuestionPostDto postDto = new QuestionPostDto(null, new String[]{"1", "2", "3", "go"},
                "just some text", 1501112360439L,
                Arrays.asList("Bob_Hoplins@epam.com", "Tyler_Greeds@epam.com",
                        "Donald_Gardner@epam.com", "Ernest_Hemingway@epam.com"));
        String authorEmail = "John_Doe@epam.com";
        questionService.addNewQuestion(postDto, authorEmail);
    }

    @Test(expected = QuestionValidationException.class)
    public void questionServiceThrowsQuestionValidationExceptionForNullTextInPostDtoTest() {
        QuestionPostDto postDto = new QuestionPostDto("just some title", new String[]{"1", "2", "3", "go"},
                null, 1501145111439L,
                Arrays.asList("Bob_Hoplins@epam.com", "Tyler_Greeds@epam.com",
                        "Donald_Gardner@epam.com", "Ernest_Hemingway@epam.com"));
        String authorEmail = "John_Doe@epam.com";
        questionService.addNewQuestion(postDto, authorEmail);
    }

    @Test(expected = QuestionValidationException.class)
    public void questionServiceThrowsQuestionValidationExceptionIfTextIsTooShortInPostDtoTest() {
        QuestionPostDto postDto = new QuestionPostDto("just some title", new String[]{"1", "2", "3", "go"},
                "", 1501145922239L,
                Arrays.asList("Bob_Hoplins@epam.com", "Tyler_Greeds@epam.com",
                        "Donald_Gardner@epam.com", "Ernest_Hemingway@epam.com"));
        String authorEmail = "John_Doe@epam.com";
        questionService.addNewQuestion(postDto, authorEmail);
    }

    @Test(expected = QuestionValidationException.class)
    public void questionServiceThrowsQuestionValidationExceptionIfTitleIsTooShortInPostDtoTest() {
        QuestionPostDto postDto = new QuestionPostDto("tle", new String[]{"1", "2", "3", "go"},
                "just some text", 1501143330439L,
                Arrays.asList("Bob_Hoplins@epam.com", "Tyler_Greeds@epam.com",
                        "Donald_Gardner@epam.com", "Ernest_Hemingway@epam.com"));
        String authorEmail = "John_Doe@epam.com";
        questionService.addNewQuestion(postDto, authorEmail);
    }

    @Test(expected = QuestionValidationException.class)
    public void addNewQuestionThrowsQuestionValidationExceptionIfQuestionPostDtoIsNullTest() {
        String authorEmail = "John_Doe@epam.com";
        questionService.addNewQuestion(null, authorEmail);
    }

    @Test(expected = QuestionValidationException.class)
    public void addNewQuestionThrowsQuestionValidationExceptionIfQuestionAuthorEmailIsNullTest() {
        questionService.addNewQuestion(new QuestionPostDto("just some title", new String[]{"1", "2", "3", "go"},
                "just some text", 1501144323239L,
                Arrays.asList("Bob_Hoplins@epam.com", "Tyler_Greeds@epam.com",
                        "Donald_Gardner@epam.com", "Ernest_Hemingway@epam.com")), null);
    }

    @Test
    public void getAvailableTagsReturnsExpectedValue() {
        List<String> actualTags = questionService.getRelevantTags(SEARCH_TAG);
        final int expectedSize = 3;

        assertThat(actualTags.size(), is(expectedSize));
        assertThat(actualTags.get(0), is(MOST_POPULAR_TAG));
    }
}