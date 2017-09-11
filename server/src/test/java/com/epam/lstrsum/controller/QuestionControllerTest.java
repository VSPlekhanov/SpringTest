package com.epam.lstrsum.controller;

import com.epam.lstrsum.dto.common.CounterDto;
import com.epam.lstrsum.dto.question.QuestionAllFieldsDto;
import com.epam.lstrsum.dto.question.QuestionAppearanceDto;
import com.epam.lstrsum.dto.question.QuestionPostDto;
import com.epam.lstrsum.dto.question.QuestionWithAnswersCountDto;
import com.epam.lstrsum.model.Question;
import com.epam.lstrsum.service.QuestionService;
import com.epam.lstrsum.service.UserService;
import com.epam.lstrsum.testutils.InstantiateUtil;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static com.epam.lstrsum.testutils.InstantiateUtil.NON_EXISTING_QUESTION_ID;
import static com.epam.lstrsum.testutils.InstantiateUtil.someInt;
import static com.epam.lstrsum.testutils.InstantiateUtil.someQuestion;
import static com.epam.lstrsum.testutils.InstantiateUtil.someQuestionAppearanceDto;
import static com.epam.lstrsum.testutils.InstantiateUtil.someQuestionPostDto;
import static com.epam.lstrsum.testutils.InstantiateUtil.someString;
import static com.epam.lstrsum.utils.FunctionalUtil.getList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
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

    @Mock
    private UserService userService;

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
        final String authorEmail = someString();
        when(userRuntimeRequestComponent.getEmail()).thenReturn(authorEmail);
        final QuestionPostDto postDto = someQuestionPostDto();
        when(questionService.addNewQuestion(any(), any())).thenReturn(someQuestion());

        controller.addQuestion(postDto);
        verify(questionService, times(1)).addNewQuestion(postDto, authorEmail);
    }

    @Test
    public void addQuestionReturnValidResponseEntityTest() throws IOException {
        final String questionId = someString();
        final Question question = Question.builder().questionId(questionId).build();

        when(questionService.addNewQuestion(any(), any())).thenReturn(question);
        when(userRuntimeRequestComponent.getEmail()).thenReturn(someString());

        assertThat(controller.addQuestion(someQuestionPostDto())).isEqualTo(ResponseEntity.ok(questionId));
    }

    @Test
    public void getQuestionsReturnsValidResponseEntityTest() {
        final List<QuestionWithAnswersCountDto> list = getList(InstantiateUtil::someQuestionWithAnswersCountDto);

        when(questionService.findAllQuestionsBaseDto(anyInt(), anyInt())).thenReturn(list);
        assertThat(controller.getQuestions(someInt(), someInt())).isEqualTo(ResponseEntity.ok(list));
    }

    @Test
    public void getQuestionsParamsCantLessThenZeroTest() {
        final int questionAmount = -5;
        final int questionPage = -4;
        final List<QuestionWithAnswersCountDto> list = Collections.emptyList();

        when(questionService.findAllQuestionsBaseDto(anyInt(), anyInt())).thenReturn(list);

        assertThat(controller.getQuestions(questionPage, questionAmount)).isEqualTo(ResponseEntity.ok(list));
    }

    @Test
    public void getQuestionWithTextShouldReturnValidResponseEntityWhenQuestionExists() throws Exception {
        final QuestionAppearanceDto dto = someQuestionAppearanceDto();

        when(questionService.contains(anyString())).thenReturn(true);
        when(questionService.getQuestionAppearanceDotByQuestionId(anyString())).thenReturn(dto);

        assertThat(controller.getQuestionWithText(someString())).isEqualTo(ResponseEntity.ok(dto));
    }

    @Test
    public void getQuestionWithAnswersShouldReturnNotFoundResponseEntityWhenSuchQuestionDoesNotExist() throws Exception {
        final String questionId = NON_EXISTING_QUESTION_ID;

        when(questionService.contains(questionId)).thenReturn(false);

        assertThat(controller.getQuestionWithText(questionId)).isEqualTo(ResponseEntity.notFound().build());
    }

    @Test
    public void searchSuccessful() {
        final List<QuestionAllFieldsDto> questionAllFieldsDtos = getList(InstantiateUtil::someQuestionAllFieldsDto);

        when(questionService.search(anyString(), anyInt(), anyInt())).thenReturn(questionAllFieldsDtos);

        assertThat(controller.search(someString(), 0, 20)).isEqualTo(ResponseEntity.ok(questionAllFieldsDtos));
    }

    @Test
    public void countSearchResult() {
        final String searchQuery = "android";
        doReturn(2L).when(questionService).getTextSearchResultsCount(searchQuery);

        ResponseEntity<CounterDto> actual = controller.searchCount(searchQuery);

        verify(questionService).getTextSearchResultsCount(eq(searchQuery));
        assertThat(actual.getStatusCode(), is(HttpStatus.OK));
        assertThat(actual.getBody().getCount(), is(2L));
    }

    @Test
    public void getRelevantTags() {
        final String keyTag = "j";
        final ResponseEntity<List<String>> actual = controller.getRelevantTags(keyTag);

        verify(questionService, times(1)).getRelevantTags(eq(keyTag));
        assertThat(actual.getStatusCode(), is(HttpStatus.OK));
    }
}
