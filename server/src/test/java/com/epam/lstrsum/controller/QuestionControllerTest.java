package com.epam.lstrsum.controller;

import com.epam.lstrsum.dto.common.CounterDto;
import com.epam.lstrsum.dto.question.*;
import com.epam.lstrsum.model.Question;
import com.epam.lstrsum.service.QuestionService;
import com.epam.lstrsum.service.UserService;
import com.epam.lstrsum.testutils.AssertionUtils;
import com.epam.lstrsum.testutils.InstantiateUtil;
import lombok.val;
import org.hamcrest.Matchers;
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
import static org.hamcrest.Matchers.isIn;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.contains;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
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
    public void addQuestionUserShouldSaveQuestion() throws IOException {
        final String authorEmail = someString();

        when(userRuntimeRequestComponent.getEmail()).thenReturn(authorEmail);
        when(userService.findUserByEmailIfExist(authorEmail)).thenReturn(Optional.of(someUser()));

        val postDto = someQuestionPostDto();
        when(questionService.addNewQuestion(any(), any(), any())).thenReturn(someQuestion());

        MultipartFile[] files = new MultipartFile[]{};

        controller.addQuestion(postDto, files);
        assertThat(authorEmail, isIn(postDto.getAllowedSubs()));
        verify(questionService, times(1)).addNewQuestion(postDto, authorEmail, files);
    }

    @Test
    public void addQuestionUserReturnValidResponseEntityTest() throws IOException {
        final String questionId = someString();
        final String authorEmail = someString();
        final Question question = Question.builder().questionId(questionId).build();

        when(userRuntimeRequestComponent.getEmail()).thenReturn(authorEmail);
        when(questionService.addNewQuestion(any(), any(), any())).thenReturn(question);
        when(userService.findUserByEmailIfExist(authorEmail)).thenReturn(Optional.of(someUser()));

        val postDto = someQuestionPostDto();
        assertThat(controller.addQuestion(postDto, new MultipartFile[]{})).isEqualTo(ResponseEntity.ok(questionId));
        assertThat(authorEmail, isIn(postDto.getAllowedSubs()));
    }

    @Test
    public void getQuestionsReturnsValidResponseEntityTest() {
        final List<QuestionWithAnswersCountDto> list = getList(InstantiateUtil::someQuestionWithAnswersCountDto);

        when(questionService.findAllQuestionsBaseDto(anyInt(), anyInt())).thenReturn(list);
        when(questionService.getQuestionCount()).thenReturn((long) list.size());
        when(userRuntimeRequestComponent.isInDistributionList()).thenReturn(true);

        controller.setMaxQuestionAmount(200);
        ResponseEntity<QuestionWithAnswersCountListDto> actual = controller.getQuestions(someInt(), someInt());

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

        ResponseEntity<QuestionWithAnswersCountListDto> actual = controller.getQuestions(questionPage, questionAmount);

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

        ResponseEntity<QuestionWithAnswersCountListDto> actual = controller.getQuestions(questionPage, questionAmount);

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
        ResponseEntity<QuestionWithAnswersCountListDto> actual = controller.getQuestions(0, 100);

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
        Long totalNumber = (long) questionAllFieldsDtos.size();

        when(questionService.search(anyString(), anyInt(), anyInt())).thenReturn(questionAllFieldsDtos);
        when(questionService.getTextSearchResultsCount(anyString())).thenReturn(totalNumber);
        when(userRuntimeRequestComponent.isInDistributionList()).thenReturn(true);

        controller.setMaxQuestionAmount(200);
        QuestionAllFieldsListDto actual = controller.search(someString(), 0, 20).getBody();
        assertThat(actual.getTotalNumber()).isEqualTo(totalNumber);
        assertThat(actual.getQuestions()).isEqualTo(questionAllFieldsDtos);
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
