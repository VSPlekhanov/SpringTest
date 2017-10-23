package com.epam.lstrsum.controller;

import com.epam.lstrsum.SetUpDataBaseCollections;
import com.epam.lstrsum.dto.common.CounterDto;
import com.epam.lstrsum.dto.question.QuestionAllFieldsDto;
import com.epam.lstrsum.dto.question.QuestionAppearanceDto;
import com.epam.lstrsum.dto.question.QuestionWithAnswersCountDto;
import com.epam.lstrsum.testutils.AssertionUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static com.epam.lstrsum.testutils.InstantiateUtil.ANOTHER_EXISTING_QUESTION_ID;
import static com.epam.lstrsum.testutils.InstantiateUtil.EXISTING_QUESTION_ID;
import static com.epam.lstrsum.testutils.InstantiateUtil.EXISTING_QUESTION_SEARCH_TEXT;
import static com.epam.lstrsum.testutils.InstantiateUtil.EXISTING_USER_EMAIL;
import static com.epam.lstrsum.testutils.InstantiateUtil.NON_EXISTING_QUESTION_ID;
import static com.epam.lstrsum.testutils.InstantiateUtil.someString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@PropertySource("classpath:application.properties")
public class QuestionRestApiTest extends SetUpDataBaseCollections {

    @Value("${question.max-question-amount}")
    private int maxQuestionAmount;

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private UserRuntimeRequestComponent userRuntimeRequestComponent;

    @Autowired
    private AnswerController answerController;

    @Test
    public void getQuestionCountTest() {
        when(userRuntimeRequestComponent.isInDistributionList()).thenReturn(true);
        Long result = restTemplate.exchange("/api/question/count", HttpMethod.GET, null, CounterDto.class).getBody().getCount();
        assertThat(result).isEqualTo(6);
    }

    @Test
    public void getQuestionCountWithAllowedSubsBobTest() {
        when(userRuntimeRequestComponent.isInDistributionList()).thenReturn(false);
        when(userRuntimeRequestComponent.getEmail()).thenReturn("Bob_Hoplins@epam.com");
        Long result = restTemplate.exchange("/api/question/count", HttpMethod.GET, null, CounterDto.class).getBody().getCount();
        assertThat(result).isEqualTo(5);
    }

    @Test
    public void getQuestionCountWithAllowedSubsTylerTest() {
        when(userRuntimeRequestComponent.isInDistributionList()).thenReturn(false);
        when(userRuntimeRequestComponent.getEmail()).thenReturn("Tyler_Greeds@epam.com");
        Long result = restTemplate.exchange("/api/question/count", HttpMethod.GET, null, CounterDto.class).getBody().getCount();
        assertThat(result).isEqualTo(5);
    }

    @Test
    public void getQuestionsReturnsValidResponseEntityTest() {
        when(userRuntimeRequestComponent.isInDistributionList()).thenReturn(true);
        String uri = String.format("/api/question/list?questionPage=%d&questionAmount=%d", 0, 2);
        List<QuestionWithAnswersCountDto> result = restTemplate.exchange(uri, HttpMethod.GET, null,
                new ParameterizedTypeReference<List<QuestionWithAnswersCountDto>>() {
                }).getBody();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getQuestionId()).isEqualTo("2u_3r");
        assertThat(result.get(1).getQuestionId()).isEqualTo("1u_2r");
    }

    @Test
    public void getQuestionsWithAllowedSubsBobReturnsValidResponseEntityTest() {
        when(userRuntimeRequestComponent.isInDistributionList()).thenReturn(false);
        when(userRuntimeRequestComponent.getEmail()).thenReturn("Bob_Hoplins@epam.com");

        String uri = String.format("/api/question/list?questionPage=%d&questionAmount=%d", 0, 5);
        List<QuestionWithAnswersCountDto> result = restTemplate.exchange(uri, HttpMethod.GET, null,
                new ParameterizedTypeReference<List<QuestionWithAnswersCountDto>>() {
                }).getBody();
        assertThat(result).hasSize(5);
        assertThat(result.get(0).getQuestionId()).isEqualTo("1u_2r");
        assertThat(result.get(1).getQuestionId()).isEqualTo("6u_6r");
        assertThat(result.get(2).getQuestionId()).isEqualTo("4u_5r");
        assertThat(result.get(3).getQuestionId()).isEqualTo("1u_1r");
        assertThat(result.get(4).getQuestionId()).isEqualTo("3u_4r");
    }

    @Test
    public void getQuestionsWithAllowedSubsTylerReturnsValidResponseEntityTest() {
        when(userRuntimeRequestComponent.isInDistributionList()).thenReturn(false);
        when(userRuntimeRequestComponent.getEmail()).thenReturn("Tyler_Greeds@epam.com");

        String uri = String.format("/api/question/list?questionPage=%d&questionAmount=%d", 0, 5);
        List<QuestionWithAnswersCountDto> result = restTemplate.exchange(uri, HttpMethod.GET, null,
                new ParameterizedTypeReference<List<QuestionWithAnswersCountDto>>() {
                }).getBody();
        assertThat(result).hasSize(5);
        assertThat(result.get(0).getQuestionId()).isEqualTo("2u_3r");
        assertThat(result.get(1).getQuestionId()).isEqualTo("1u_2r");
        assertThat(result.get(2).getQuestionId()).isEqualTo("6u_6r");
        assertThat(result.get(3).getQuestionId()).isEqualTo("4u_5r");
        assertThat(result.get(4).getQuestionId()).isEqualTo("1u_1r");
    }

    @Test
    public void getQuestionWithTextShouldReturnValidResponseEntityWhenQuestionExists() throws Exception {
        String questionId = EXISTING_QUESTION_ID;
        String uri = String.format("/api/question/%s", questionId);

        when(userRuntimeRequestComponent.isInDistributionList()).thenReturn(true);
        ResponseEntity<QuestionAppearanceDto> result = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<QuestionAppearanceDto>() {
                });

        assertThat(result).satisfies(AssertionUtils::hasStatusOk);
        assertThat(result.getBody().getQuestionId()).isEqualTo(questionId);
    }

    @Test
    public void getQuestionWithAnswersShouldReturnNotFoundResponseEntityWhenSuchQuestionDoesNotExist() throws Exception {
        String uri = String.format("/api/question/%s", NON_EXISTING_QUESTION_ID);
        assertThat(restTemplate.exchange(uri, HttpMethod.GET, null, Object.class))
                .satisfies(AssertionUtils::hasStatusNotFound);
    }

    @Test
    public void searchWithDistributionListUserSuccessful() {
        String uri = String.format("/api/question/search?query=%s&page=%d&size=%d", EXISTING_QUESTION_SEARCH_TEXT, 0, 2);
        when(userRuntimeRequestComponent.isInDistributionList()).thenReturn(true);

        ResponseEntity<List<QuestionAllFieldsDto>> result = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<QuestionAllFieldsDto>>() {
                });

        assertThat(result).satisfies(AssertionUtils::hasStatusOk);
        assertThat(result.getBody().size()).isEqualTo(1);
        assertThat(result.getBody().get(0).getQuestionId()).isEqualTo(EXISTING_QUESTION_ID);
    }

    @Test
    public void searchWithAllowedSubUserSuccessful() {
        String uri = String.format("/api/question/search?query=%s&page=%d&size=%d", EXISTING_QUESTION_SEARCH_TEXT, 0, 2);
        when(userRuntimeRequestComponent.isInDistributionList()).thenReturn(false);
        when(userRuntimeRequestComponent.getEmail()).thenReturn(EXISTING_USER_EMAIL);

        ResponseEntity<List<QuestionAllFieldsDto>> result = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<QuestionAllFieldsDto>>() {
                });

        assertThat(result).satisfies(AssertionUtils::hasStatusOk);
        assertThat(result.getBody().size()).isEqualTo(1);
        assertThat(result.getBody().get(0).getQuestionId()).isEqualTo(EXISTING_QUESTION_ID);
    }

    @Test
    public void searchFailed() {
        String uri = String.format("/api/question/search?query=%s&page=%d&size=%d", someString(), 0, 2);
        when(userRuntimeRequestComponent.isInDistributionList()).thenReturn(true);

        ResponseEntity<List<QuestionAllFieldsDto>> result = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<QuestionAllFieldsDto>>() {
                });

        assertThat(result).satisfies(AssertionUtils::hasStatusOk);
        assertThat(result.getBody().size()).isEqualTo(0);
    }

    @Test
    public void smartSearchSuccessful() throws Exception {
        String uri = String.format("/api/question/smartSearch?query=%s&page=%d&size=%d", someString(), 0, 2);
        when(userRuntimeRequestComponent.isInDistributionList()).thenReturn(true);

        assertThat(restTemplate.exchange(uri, HttpMethod.GET, null, Object.class))
                .satisfies(AssertionUtils::hasStatusOk);
    }

    @Test
    public void smartSearchWithAllowedSubThrowException() throws Exception {
        String uri = String.format("/api/question/smartSearch?query=%s&page=%d&size=%d", someString(), 0, 2);
        when(userRuntimeRequestComponent.isInDistributionList()).thenReturn(false);
        when(userRuntimeRequestComponent.getEmail()).thenReturn(someString());

        ResponseEntity<String> result = restTemplate.exchange(uri, HttpMethod.GET, null, String.class);

        assertThat(result)
                .satisfies(AssertionUtils::hasStatusInternalServerError);
    }

    @Test
    public void countSearchResult() {
        String uri = String.format("/api/question/search/count?query=%s", "postman");
        when(userRuntimeRequestComponent.isInDistributionList()).thenReturn(true);

        ResponseEntity<CounterDto> result = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<CounterDto>() {
                });

        assertThat(result).satisfies(AssertionUtils::hasStatusOk);
        assertThat(result.getBody().getCount()).isEqualTo(1); // 1 question with text "...postman..."
    }

    @Test
    public void countSearchFailed() {
        String uri = String.format("/api/question/search/count?query=%s", someString());
        when(userRuntimeRequestComponent.getEmail()).thenReturn(EXISTING_USER_EMAIL);

        ResponseEntity<CounterDto> result = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<CounterDto>() {
                });

        assertThat(result).satisfies(AssertionUtils::hasStatusOk);
        assertThat(result.getBody().getCount()).isEqualTo(0);
    }

    @Test
    public void getRelevantTags() {
        String uri = String.format("/api/question/getRelevantTags?key=%s", "java");
        when(userRuntimeRequestComponent.isInDistributionList()).thenReturn(true);

        ResponseEntity<List<String>> result = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<String>>() {
                });

        assertThat(result).satisfies(AssertionUtils::hasStatusOk);
        assertThat(result.getBody()).hasSize(2);
        assertThat(result.getBody().get(0)).isEqualTo("javascript");
        assertThat(result.getBody().get(1)).isEqualTo("java");
    }

    @Test
    public void getRelevantTagsWithNotDistributionListUser() {
        String uri = String.format("/api/question/getRelevantTags?key=%s", someString());
        when(userRuntimeRequestComponent.isInDistributionList()).thenReturn(false);

        ResponseEntity<List<String>> result = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<String>>() {
                });

        assertThat(result).satisfies(AssertionUtils::hasStatusNotFound);
    }

    @Test
    public void getRelevantTagsNonExistingTags() {
        String uri = String.format("/api/question/getRelevantTags?key=%s", someString());
        when(userRuntimeRequestComponent.isInDistributionList()).thenReturn(true);

        ResponseEntity<List<String>> result = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<String>>() {
                });

        assertThat(result).satisfies(AssertionUtils::hasStatusOk);
        assertThat(result.getBody()).hasSize(0);
    }

    @Test
    public void getAnswerCountByQuestionId() {
        long answerCount1 = answerController.getAnswerCountByQuestionId(EXISTING_QUESTION_ID).getBody().getCount();
        long answerCount2 = answerController.getAnswerCountByQuestionId(ANOTHER_EXISTING_QUESTION_ID).getBody().getCount();
        assertThat(answerCount1).isEqualTo(3L);
        assertThat(answerCount2).isEqualTo(2L);
    }
}