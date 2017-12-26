package com.epam.lstrsum.controller;

import com.epam.lstrsum.SetUpDataBaseCollections;
import com.epam.lstrsum.model.User;
import com.epam.lstrsum.service.QuestionService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class SubscriptionControllerTest extends SetUpDataBaseCollections {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private QuestionService questionService;

    @Test
    public void subscribeFromDlTest() {
        subscribeTest("6u_6r", "Donald_Gardner@epam.com", true);
    }

    @Test
    public void unsubscribeFromDlTest() {
        unsubscribeTest("6u_6r", "john_doe@epam.com", false);
    }

    @Test
    public void subscribeFromAllowedSubTest() {
        subscribeTest("6u_6r", "Bob_Hoplins@epam.com", true);
    }

    @Test
    public void unsubscribeFromAllowedSubTest() {
        unsubscribeTest("6u_6r", "tyler_greeds@epam.com", false);
    }

    private void subscribeTest(String questionId, String userEmail, boolean isInDistributionList) {
        List<User> listBeforeUpdate = questionService.getQuestionById(questionId).getSubscribers();

        doReturn(userEmail).when(userRuntimeRequestComponent).getEmail();
        when(userRuntimeRequestComponent.isInDistributionList()).thenReturn(isInDistributionList);

        ResponseEntity<Object> exchange = restTemplate.exchange(
                String.format("/api/subscription/subscribe/%s", questionId),
                HttpMethod.PUT, RequestEntity.EMPTY, Object.class
        );

        assertThat(exchange.getStatusCode()).isEqualByComparingTo(HttpStatus.NO_CONTENT);
        assertThat(questionService.getQuestionById(questionId).getSubscribers())
                .hasSize(listBeforeUpdate.size() + 1);
        verify(userRuntimeRequestComponent, times(1)).getEmail();
    }

    private void unsubscribeTest(String questionId, String userEmail, boolean isInDistributionList) {
        List<User> listBeforeUpdate = questionService.getQuestionById(questionId).getSubscribers();

        doReturn(userEmail).when(userRuntimeRequestComponent).getEmail();
        when(userRuntimeRequestComponent.isInDistributionList()).thenReturn(isInDistributionList);

        ResponseEntity<Object> exchange = restTemplate.exchange(
                String.format("/api/subscription/unsubscribe/%s", questionId),
                HttpMethod.PUT, RequestEntity.EMPTY, Object.class
        );

        assertThat(exchange.getStatusCode()).isEqualByComparingTo(HttpStatus.NO_CONTENT);
        assertThat(questionService.getQuestionById(questionId).getSubscribers())
                .hasSize(listBeforeUpdate.size() - 1);
        verify(userRuntimeRequestComponent, times(1)).getEmail();
    }

}