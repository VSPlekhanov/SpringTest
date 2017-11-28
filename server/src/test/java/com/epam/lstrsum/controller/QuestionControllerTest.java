package com.epam.lstrsum.controller;

import com.epam.lstrsum.dto.common.CounterDto;
import com.epam.lstrsum.dto.question.QuestionAllFieldsDto;
import com.epam.lstrsum.dto.question.QuestionAppearanceDto;
import com.epam.lstrsum.dto.question.QuestionListDto;
import com.epam.lstrsum.dto.question.QuestionWithAnswersCountDto;
import com.epam.lstrsum.model.Question;
import com.epam.lstrsum.service.QuestionService;
import com.epam.lstrsum.service.UserService;
import com.epam.lstrsum.testutils.AssertionUtils;
import com.epam.lstrsum.testutils.InstantiateUtil;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.epam.lstrsum.testutils.InstantiateUtil.*;
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

        verify(questionService, times(1)).getQuestionCountWithAllowedSub(anyString());
    }

    @Test
    public void addQuestionWithDistributionListUserShouldSaveQuestion() throws IOException {
        val authorEmail = someString();
        when(userRuntimeRequestComponent.getEmail()).thenReturn(authorEmail);
        when(userRuntimeRequestComponent.isInDistributionList()).thenReturn(true);
        val postDto = someQuestionPostDto();
        when(questionService.addNewQuestion(any(), any(), any())).thenReturn(someQuestion());

        MultipartFile[] files = new MultipartFile[]{};

        controller.addQuestion(postDto, files);
        verify(questionService, times(1)).addNewQuestion(postDto, authorEmail, files);
    }

    @Test
    public void addQuestionWithDistributionListUserReturnValidResponseEntityTest() throws IOException {
        final String questionId = someString();
        final Question question = Question.builder().questionId(questionId).build();

        when(questionService.addNewQuestion(any(), any(), any())).thenReturn(question);
        when(userRuntimeRequestComponent.getEmail()).thenReturn(someString());
        when(userRuntimeRequestComponent.isInDistributionList()).thenReturn(true);

        assertThat(controller.addQuestion(someQuestionPostDto(), new MultipartFile[]{})).isEqualTo(ResponseEntity.ok(questionId));
    }

    @Test
    public void addQuestionWithNotDistributionListUserReturnValidResponseEntityTest() throws IOException {
        when(userRuntimeRequestComponent.getEmail()).thenReturn(someString());
        when(userRuntimeRequestComponent.isInDistributionList()).thenReturn(false);

        assertThat(controller.addQuestion(someQuestionPostDto(), new MultipartFile[]{})).satisfies(AssertionUtils::hasStatusNotFound);
    }

    @Test
    public void getQuestionsReturnsValidResponseEntityTest() {
        final List<QuestionWithAnswersCountDto> list = getList(InstantiateUtil::someQuestionWithAnswersCountDto);

        when(questionService.findAllQuestionsBaseDto(anyInt(), anyInt())).thenReturn(list);
        when(questionService.getQuestionCount()).thenReturn((long) list.size());
        when(userRuntimeRequestComponent.isInDistributionList()).thenReturn(true);

        controller.setMaxQuestionAmount(200);
        ResponseEntity<QuestionListDto> actual = controller.getQuestions(someInt(), someInt());

        assertThat(actual.getStatusCode(), is(HttpStatus.OK));
        assertThat(actual.getBody().getTotalNumber(), is((long) list.size()));
        assertThat(actual.getBody().getQuestions()).isEqualTo(list);
    }

    @Test
    public void getQuestionsParamsCantLessThenZeroTest() {
        final int questionAmount = -5;
        final int questionPage = -4;
        final List<QuestionWithAnswersCountDto> list = Collections.emptyList();

        when(questionService.findAllQuestionsBaseDto(anyInt(), anyInt())).thenReturn(list);
        when(questionService.getQuestionCount()).thenReturn((long) list.size());
        when(userRuntimeRequestComponent.isInDistributionList()).thenReturn(true);

        ResponseEntity<QuestionListDto> actual = controller.getQuestions(questionPage, questionAmount);

        assertThat(actual.getStatusCode(), is(HttpStatus.OK));
        assertThat(actual.getBody().getTotalNumber(), is((long) list.size()));
        assertThat(actual.getBody().getQuestions()).isEqualTo(list);
    }

    @Test
    public void getQuestionsParamsCantBeTooBigTest() {
        final int questionAmount = Integer.MAX_VALUE;
        final int questionPage = Integer.MAX_VALUE;
        final List<QuestionWithAnswersCountDto> list = Collections.emptyList();

        when(questionService.findAllQuestionsBaseDto(anyInt(), anyInt())).thenReturn(list);
        when(questionService.getQuestionCount()).thenReturn((long) list.size());
        when(userRuntimeRequestComponent.isInDistributionList()).thenReturn(true);

        ResponseEntity<QuestionListDto> actual = controller.getQuestions(questionPage, questionAmount);

        assertThat(actual.getStatusCode(), is(HttpStatus.OK));
        assertThat(actual.getBody().getTotalNumber(), is((long) list.size()));
        assertThat(actual.getBody().getQuestions()).isEqualTo(list);
    }

    @Test
    public void getQuestionsShouldInteractWithQuestionServiceGetWithAllowedSub() {
        val list = getList(InstantiateUtil::someQuestionWithAnswersCountDto);
        final List<QuestionWithAnswersCountDto> emptyList = Collections.emptyList();

        when(questionService.findAllQuestionsBaseDto(anyInt(), anyInt())).thenReturn(emptyList);
        when(questionService.findAllQuestionBaseDtoWithAllowedSub(anyInt(), anyInt(), anyString())).thenReturn(list);
        when(questionService.getQuestionCount()).thenReturn(0L);
        when(questionService.getQuestionCountWithAllowedSub(anyString())).thenReturn((long) list.size());
        when(userRuntimeRequestComponent.isInDistributionList()).thenReturn(false);

        controller.setMaxQuestionAmount(200);
        ResponseEntity<QuestionListDto> actual = controller.getQuestions(0, 100);

        assertThat(actual).satisfies(AssertionUtils::hasStatusOk).satisfies(
                listResponseEntity -> assertThat(listResponseEntity.getBody().getTotalNumber(), is((long) list.size()))
        );

        assertThat(actual).satisfies(
                listResponseEntity -> assertThat(listResponseEntity.getBody().getQuestions()).isEqualTo(list)
        );

        verify(questionService, times(0))
                .getQuestionCount();
        verify(questionService, times(1))
                .getQuestionCountWithAllowedSub(anyString());
        verify(questionService, times(0))
                .findAllQuestionsBaseDto(anyInt(), anyInt());
        verify(questionService, times(1))
                .findAllQuestionBaseDtoWithAllowedSub(anyInt(), anyInt(), anyString());
    }

    @Test
    public void getQuestionWithTextShouldReturnValidResponseEntityWhenQuestionExists() throws Exception {
        final QuestionAppearanceDto dto = someQuestionAppearanceDto();

        when(questionService.contains(anyString())).thenReturn(true);
        when(questionService.getQuestionAppearanceDtoByQuestionId(anyString(), anyString())).thenReturn(Optional.of(dto));
        when(userRuntimeRequestComponent.isInDistributionList()).thenReturn(true);

        assertThat(controller.getQuestionWithText(someString())).isEqualTo(ResponseEntity.ok(dto));
    }

    @Test
    public void getQuestionWithAnswersShouldReturnNotFoundResponseEntityWhenSuchQuestionDoesNotExist() throws Exception {
        final String questionId = NON_EXISTING_QUESTION_ID;
        final String userEmail = someString();

        when(userRuntimeRequestComponent.getEmail()).thenReturn(userEmail);
        when(questionService.getQuestionAppearanceDtoByQuestionId(questionId, userEmail)).thenReturn(Optional.empty());
        when(userRuntimeRequestComponent.isInDistributionList()).thenReturn(true);

        assertThat(controller.getQuestionWithText(questionId)).isEqualTo(ResponseEntity.notFound().build());
    }

    @Test
    public void searchSuccessful() {
        final List<QuestionAllFieldsDto> questionAllFieldsDtos = getList(InstantiateUtil::someQuestionAllFieldsDto);

        when(questionService.search(anyString(), anyInt(), anyInt())).thenReturn(questionAllFieldsDtos);
        when(userRuntimeRequestComponent.isInDistributionList()).thenReturn(true);

        assertThat(controller.search(someString(), 0, 20)).isEqualTo(ResponseEntity.ok(questionAllFieldsDtos));
    }

    @Test
    public void countSearchResult() {
        final String searchQuery = "android";
        doReturn(2L).when(questionService).getTextSearchResultsCount(searchQuery);
        when(userRuntimeRequestComponent.isInDistributionList()).thenReturn(true);

        ResponseEntity<CounterDto> actual = controller.searchCount(searchQuery);

        verify(questionService).getTextSearchResultsCount(eq(searchQuery));
        assertThat(actual.getStatusCode(), is(HttpStatus.OK));
        assertThat(actual.getBody().getCount(), is(2L));
    }

    @Test
    public void getRelevantTags() {
        final String keyTag = "j";
        when(userRuntimeRequestComponent.isInDistributionList()).thenReturn(true);
        final ResponseEntity<List<String>> actual = controller.getRelevantTags(keyTag);

        verify(questionService, times(1)).getRelevantTags(eq(keyTag));
        assertThat(actual.getStatusCode(), is(HttpStatus.OK));
    }
}
