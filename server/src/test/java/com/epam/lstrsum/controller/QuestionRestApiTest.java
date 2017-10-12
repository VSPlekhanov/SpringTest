package com.epam.lstrsum.controller;

import com.epam.lstrsum.SetUpDataBaseCollections;
import com.epam.lstrsum.service.QuestionService;
import com.epam.lstrsum.service.UserService;
import com.epam.lstrsum.testutils.AssertionUtils;
import com.epam.lstrsum.testutils.InstantiateUtil;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpMethod;

import java.util.Optional;

import static com.epam.lstrsum.testutils.InstantiateUtil.ANOTHER_EXISTING_QUESTION_ID;
import static com.epam.lstrsum.testutils.InstantiateUtil.EXISTING_QUESTION_ID;
import static com.epam.lstrsum.testutils.InstantiateUtil.NON_EXISTING_QUESTION_ID;
import static com.epam.lstrsum.testutils.InstantiateUtil.someInt;
import static com.epam.lstrsum.testutils.InstantiateUtil.someLong;
import static com.epam.lstrsum.testutils.InstantiateUtil.someQuestion;
import static com.epam.lstrsum.testutils.InstantiateUtil.someQuestionAppearanceDto;
import static com.epam.lstrsum.testutils.InstantiateUtil.someString;
import static com.epam.lstrsum.utils.FunctionalUtil.getList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@PropertySource("classpath:application.properties")
public class QuestionRestApiTest extends SetUpDataBaseCollections {

    @Value("${question.max-question-amount}")
    private int maxQuestionAmount;

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private QuestionService questionService;

    @MockBean
    private UserRuntimeRequestComponent userRuntimeRequestComponent;

    @MockBean
    private UserService userService;

    @Autowired
    private AnswerController answerController;

    @Test
    public void getQuestionCount() {
        when(questionService.getQuestionCount()).thenReturn(someLong());
        assertThat(restTemplate.exchange("/api/question/count", HttpMethod.GET, null, Object.class))
                .satisfies(AssertionUtils::hasStatusOk);
        verify(questionService, times(1)).getQuestionCountWithAllowedSub(anyString());
    }

    @Test
    public void getQuestionsReturnsValidResponseEntityTest() {

        when(userRuntimeRequestComponent.isInDistributionList()).thenReturn(true);
        when(questionService.findAllQuestionsBaseDto(someInt(), someInt()))
                .thenReturn(getList(InstantiateUtil::someQuestionWithAnswersCountDto));

        String uri = String.format("/api/question/list?questionPage=%d&questionAmount=%d", someInt(), someInt());
        assertThat(restTemplate.exchange(uri, HttpMethod.GET, null, Object.class))
                .satisfies(AssertionUtils::hasStatusOk);
        verify(questionService, times(1)).findAllQuestionsBaseDto(anyInt(), anyInt());
    }

    @Test
    public void getQuestionsWithAllowedSubReturnsValidResponseEntityTest() {

        when(userRuntimeRequestComponent.isInDistributionList()).thenReturn(false);
        when(questionService.findAllQuestionBaseDtoWithAllowedSub(someInt(), someInt(), someString()))
                .thenReturn(getList(InstantiateUtil::someQuestionWithAnswersCountDto));

        String uri = String.format("/api/question/list?questionPage=%d&questionAmount=%d", someInt(), someInt());
        assertThat(restTemplate.exchange(uri, HttpMethod.GET, null, Object.class))
                .satisfies(AssertionUtils::hasStatusOk);
        verify(questionService, times(1)).findAllQuestionBaseDtoWithAllowedSub(anyInt(), anyInt(), anyString());
    }

    @Test
    public void getQuestionsInvalidParams() {

        when(userRuntimeRequestComponent.isInDistributionList()).thenReturn(true);
        when(questionService.findAllQuestionsBaseDto(anyInt(), anyInt()))
                .thenReturn(getList(InstantiateUtil::someQuestionWithAnswersCountDto));

        String uri = String.format("/api/question/list?questionPage=%d&questionAmount=%d", -4, maxQuestionAmount + 1);
        assertThat(restTemplate.exchange(uri, HttpMethod.GET, null, Object.class))
                .satisfies(AssertionUtils::hasStatusOk);

        verify(questionService, times(1)).findAllQuestionsBaseDto(0, maxQuestionAmount);

    }

    @Test
    public void getQuestionsWithAllowedSubInvalidParams() {

        when(userRuntimeRequestComponent.isInDistributionList()).thenReturn(false);
        when(questionService.findAllQuestionBaseDtoWithAllowedSub(anyInt(), anyInt(), anyString()))
                .thenReturn(getList(InstantiateUtil::someQuestionWithAnswersCountDto));

        String uri = String.format("/api/question/list?questionPage=%d&questionAmount=%d", -4, maxQuestionAmount + 1);
        assertThat(restTemplate.exchange(uri, HttpMethod.GET, null, Object.class))
                .satisfies(AssertionUtils::hasStatusOk);

        verify(questionService, times(1))
                .findAllQuestionBaseDtoWithAllowedSub(eq(0), eq(maxQuestionAmount), anyString());

    }

    @Test
    public void getQuestionWithTextShouldReturnValidResponseEntityWhenQuestionExists() throws Exception {
        String questionId = someQuestion().getQuestionId();

        when(questionService.contains(questionId)).thenReturn(true);
        when(questionService.getQuestionAppearanceDtoByQuestionId(questionId)).thenReturn(Optional.of(someQuestionAppearanceDto()));

        String uri = String.format("/api/question/%s", questionId);
        assertThat(restTemplate.exchange(uri, HttpMethod.GET, null, Object.class))
                .satisfies(AssertionUtils::hasStatusOk);
        verify(questionService, times(1)).getQuestionAppearanceDtoByQuestionId(questionId);

    }

    @Test
    public void getQuestionWithAnswersShouldReturnNotFoundResponseEntityWhenSuchQuestionDoesNotExist() throws Exception {
        final String questionId = NON_EXISTING_QUESTION_ID;
        when(questionService.getQuestionAppearanceDtoByQuestionId(questionId)).thenReturn(Optional.empty());

        String uri = String.format("/api/question/%s", questionId);
        assertThat(restTemplate.exchange(uri, HttpMethod.GET, null, Object.class))
                .satisfies(AssertionUtils::hasStatusNotFound);

        verify(questionService, times(1)).getQuestionAppearanceDtoByQuestionId(questionId);
    }

    @Test
    public void searchSuccessful() {

        when(questionService.search(someString(), someInt(), someInt())).thenReturn(getList(InstantiateUtil::someQuestionAllFieldsDto));

        String uri = String.format("/api/question/search?query=%s&page=%d&size=%d", someString(), someInt(), someInt());
        assertThat(restTemplate.exchange(uri, HttpMethod.GET, null, Object.class))
                .satisfies(AssertionUtils::hasStatusOk);

        verify(questionService, times(1)).search(anyString(), anyInt(), anyInt());
    }

    @Test
    public void smartSearchSuccessful() throws Exception {

        when(questionService.smartSearch(someString(), someInt(), someInt())).thenReturn(someString());

        String uri = String.format("/api/question/smartSearch?query=%s&page=%d&size=%d", someString(), someInt(), someInt());
        assertThat(restTemplate.exchange(uri, HttpMethod.GET, null, Object.class))
                .satisfies(AssertionUtils::hasStatusOk);

        verify(questionService, times(1)).smartSearch(anyString(), anyInt(), anyInt());
    }

    @Test
    public void countSearchResult() {
        String query = someString();

        when(questionService.getTextSearchResultsCount(anyString())).thenReturn(2L);

        String uri = String.format("/api/question/search/count?query=%s", query);
        assertThat(restTemplate.exchange(uri, HttpMethod.GET, null, Object.class))
                .satisfies(AssertionUtils::hasStatusOk);

        verify(questionService, times(1)).getTextSearchResultsCount(query);
    }

    @Test
    public void getRelevantTags() {
        String key = someString();

        when(questionService.getRelevantTags(anyString())).thenReturn(getList(InstantiateUtil::someString));

        String uri = String.format("/api/question/getRelevantTags?key=%s", key);
        assertThat(restTemplate.exchange(uri, HttpMethod.GET, null, Object.class))
                .satisfies(AssertionUtils::hasStatusOk);

        verify(questionService, times(1)).getRelevantTags(key);
    }

    @Test
    public void getAnswerCountByQuestionId() {
        long answerCount1 = answerController.getAnswerCountByQuestionId(EXISTING_QUESTION_ID).getBody().getCount();
        long answerCount2 = answerController.getAnswerCountByQuestionId(ANOTHER_EXISTING_QUESTION_ID).getBody().getCount();
        assertThat(answerCount1).isEqualTo(3L);
        assertThat(answerCount2).isEqualTo(2L);
    }
}