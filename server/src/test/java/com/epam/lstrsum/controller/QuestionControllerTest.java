package com.epam.lstrsum.controller;

import com.epam.lstrsum.dto.answer.AnswerBaseDto;
import com.epam.lstrsum.dto.common.CounterDto;
import com.epam.lstrsum.dto.question.QuestionAllFieldsDto;
import com.epam.lstrsum.dto.question.QuestionAppearanceDto;
import com.epam.lstrsum.dto.question.QuestionPostDto;
import com.epam.lstrsum.dto.question.QuestionWithAnswersCountDto;
import com.epam.lstrsum.dto.user.UserBaseDto;
import com.epam.lstrsum.model.Question;
import com.epam.lstrsum.service.QuestionService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class QuestionControllerTest {

    @Mock
    private QuestionService questionService;

    @Mock
    private UserRuntimeRequestComponent userRuntimeRequestComponent;

    @InjectMocks
    private QuestionController controller;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    @Test
    public void getQuestionCount() {
        controller.getQuestionCount();

        verify(questionService, times(1)).getQuestionCount();
    }

    @Test
    public void addQuestionShouldSaveQuestionTest() throws IOException {
        String authorEmail = "John_Doe@epam.com";
        QuestionPostDto postDto = new QuestionPostDto("some title", new String[]{"1", "2", "3", "4"}, "some txet",
                1501140060439L, Collections.singletonList("Bob_Hoplins@epam.com"), Collections.emptyList());
        when(userRuntimeRequestComponent.getEmail()).thenReturn("John_Doe@epam.com");

        String questionId = "Id11";
        Question dtoWithId = Question.builder().questionId(questionId).build();
        when(questionService.addNewQuestion(postDto, authorEmail)).thenReturn(dtoWithId);

        controller.addQuestion(postDto);

        verify(questionService).addNewQuestion(postDto, authorEmail);
    }

    @Test
    public void addQuestionReturnValidResponseEntityTest() throws IOException {
        String authorEmail = "John_Doe@epam.com";
        QuestionPostDto postDto = new QuestionPostDto("some title", new String[]{"1", "2", "3", "4"}, "some txet",
                1501145960400L, Collections.singletonList("Bob_Hoplins@epam.com"), Collections.emptyList());

        String questionId = "Id11";
        Question dtoWithId = Question.builder().questionId(questionId).build();
        when(questionService.addNewQuestion(postDto, authorEmail)).thenReturn(dtoWithId);

        when(userRuntimeRequestComponent.getEmail()).thenReturn("John_Doe@epam.com");
        ResponseEntity<String> actualEntity = controller.addQuestion(postDto);

        ResponseEntity<String> expectedEntity = ResponseEntity.ok(questionId);

        assertThat(actualEntity, is(equalTo(expectedEntity)));
    }

    @Test
    public void getQuestionsReturnsValidResponseEntityTest() {
        int questionAmount = 15;
        int questionPage = 4;
        controller.setMaxQuestionAmount(questionAmount);
        List<QuestionWithAnswersCountDto> list = Arrays.asList(
                new QuestionWithAnswersCountDto("u1", "some title 2", null,
                        Instant.now(), Instant.now(),
                        new UserBaseDto("some user id 2", "first name", "last name", "some@email.com"),
                        1, 7),
                new QuestionWithAnswersCountDto("u2", "some title 2", null,
                        Instant.now(), Instant.now(),
                        new UserBaseDto("some user id 2", "first name", "last name", "some@email.com"),
                        1, 8)
        );
        when(questionService.findAllQuestionsBaseDto(questionPage, questionAmount)).thenReturn(list);
        ResponseEntity<List<QuestionWithAnswersCountDto>> actualEntity = controller.getQuestions(questionPage, questionAmount);
        ResponseEntity<List<QuestionWithAnswersCountDto>> expectedEntity = ResponseEntity.ok(list);
        assertThat(actualEntity, is(equalTo(expectedEntity)));
    }

    @Test
    public void getQuestionsParamsCantLessThenZeroTest() {
        int maxQuestionAmount = 15;
        int minQuestionPage = 0;

        int questionAmount = -5;
        int questionPage = -4;
        controller.setMaxQuestionAmount(maxQuestionAmount);
        List<QuestionWithAnswersCountDto> list = Collections.emptyList();
        when(questionService.findAllQuestionsBaseDto(minQuestionPage, maxQuestionAmount)).thenReturn(list);

        ResponseEntity<List<QuestionWithAnswersCountDto>> actualEntity = controller.getQuestions(questionPage, questionAmount);
        ResponseEntity<List<QuestionWithAnswersCountDto>> expectedEntity = ResponseEntity.ok(list);

        assertThat(actualEntity, is(equalTo(expectedEntity)));
    }

    @Test
    public void getQuestionWithAnswersShouldReturnValidResponseEntityWhenQuestionExists() throws Exception {
        String questionId = "questionId";

        QuestionAppearanceDto questionAppearanceDto = new QuestionAppearanceDto(
                questionId, "questionTitle", new String[]{"tag1", "tag2", "tag3"},
                Instant.now(), Instant.now(),
                new UserBaseDto("userId", "userName", "userSurname", "user@epam.com"),
                2, "question body",
                Arrays.asList(new AnswerBaseDto("answer1Text", Instant.now(),
                                new UserBaseDto("user1Id", "user1Name", "user1Surname", "user1@epam.com"), 6),
                        new AnswerBaseDto("answer2Text", Instant.now(),
                                new UserBaseDto("user2Id", "user2Name", "user2Surname", "user2@epam.com"), 3)));

        when(questionService.contains(questionId)).thenReturn(true);
        when(questionService.getQuestionAppearanceDotByQuestionId(questionId)).thenReturn(questionAppearanceDto);

        ResponseEntity actual = controller.getQuestionWithAnswers(questionId);
        ResponseEntity expected = ResponseEntity.ok(questionAppearanceDto);

        assertThat(actual, is(equalTo(expected)));
    }

    @Test
    public void getQuestionWithAnswersShouldReturnNotFoundResponseEntityWhenSuchQuestionDoesNotExist() throws Exception {
        String questionId = "thisQuestionDoesNotExistInDb";

        when(questionService.contains(questionId)).thenReturn(false);

        ResponseEntity actual = controller.getQuestionWithAnswers(questionId);
        ResponseEntity expected = new ResponseEntity(HttpStatus.NOT_FOUND);

        assertThat(actual, is(equalTo(expected)));
    }

    @Test
    public void searchSuccessful() {
        String questionId = "questionId";
        String searchString = "search";
        String questionTitle = "questionTitle";

        List<QuestionAllFieldsDto> questionAllFieldsDtos = Collections.singletonList(
                new QuestionAllFieldsDto(questionId, questionTitle, new String[]{"tag1", "tag2", "tag3"}, Instant.now(), Instant.now(),
                        new UserBaseDto("userId", "userName", "userSurname", "user@epam.com"),
                        2, Collections.emptyList(), "text")
        );
        when(questionService.search(searchString, 0, 20)).thenReturn(questionAllFieldsDtos);

        ResponseEntity<List<QuestionAllFieldsDto>> actual = controller.search(searchString, 0, 20);
        ResponseEntity<List<QuestionAllFieldsDto>> expected = ResponseEntity.ok(questionAllFieldsDtos);

        assertThat(actual, equalTo(expected));
    }

    @Test
    public void countSearchResult() {
        String searchQuery = "android";
        doReturn(2L).when(questionService).getTextSearchResultsCount(searchQuery);

        ResponseEntity<CounterDto> actual = controller.searchCount(searchQuery);

        verify(questionService).getTextSearchResultsCount(eq(searchQuery));
        assertThat(actual.getStatusCode(), is(HttpStatus.OK));
        assertThat(actual.getBody().getCount(), is(2L));
    }

    @Test
    public void getRelevantTags() {
        final String keyTag = "j";
        ResponseEntity<List<String>> actual = controller.getRelevantTags(keyTag);

        verify(questionService, times(1)).getRelevantTags(eq(keyTag));
        assertThat(actual.getStatusCode(), is(HttpStatus.OK));
    }
}
