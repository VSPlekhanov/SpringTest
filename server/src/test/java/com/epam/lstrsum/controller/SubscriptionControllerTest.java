package com.epam.lstrsum.controller;

import com.epam.lstrsum.SetUpDataBaseCollections;
import com.epam.lstrsum.service.SubscriptionService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class SubscriptionControllerTest extends SetUpDataBaseCollections {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private SubscriptionService subscriptionService;

    @MockBean
    private UserRuntimeRequestComponent userRuntimeRequestComponent;

    @Test
    public void subscribeTest() {
        String someUserEmail = "Bob_Hoplins@epam.com";
        doReturn(someUserEmail).when(userRuntimeRequestComponent).getEmail();

        String someQuestionId = "1u_1r";
        ResponseEntity<Object> exchange = testRestTemplate.exchange(
                "/api/subscription/" + someQuestionId, HttpMethod.PUT, RequestEntity.EMPTY, Object.class
        );

        assertThat(exchange.getStatusCode()).isEqualByComparingTo(HttpStatus.NO_CONTENT);

        assertThat(subscriptionService.findAllSubscriptionsEntitiesToQuestionWithThisId(someQuestionId))
                .anySatisfy(
                        subscription -> assertThat(subscription.getUserId().getEmail()).isEqualTo(someUserEmail)
                );

        verify(userRuntimeRequestComponent, times(1)).getEmail();
    }
}