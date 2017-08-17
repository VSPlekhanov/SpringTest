package com.epam.lstrsum.controller;

import com.epam.lstrsum.SetUpDataBaseCollections;
import com.epam.lstrsum.model.Subscription;
import com.epam.lstrsum.model.User;
import com.epam.lstrsum.service.SubscriptionService;
import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;
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
    public void getListOfSubscriptions() throws Exception {
        final ResponseEntity<List<Subscription>> responseEntity = testRestTemplate.exchange("/api/subscription",
                HttpMethod.GET, null, new ParameterizedTypeReference<List<Subscription>>() {
                });
        List<Subscription> actualList = responseEntity.getBody();
        //validate
        assertThat(actualList).hasSize(6);

        List<String> actualIds = actualList.stream()
                .map(Subscription::getUserId)
                .map(User::getUserId)
                .collect(collectingAndThen(toList(), ImmutableList::copyOf));

        assertThat(actualIds).containsExactlyInAnyOrder("1u", "2u", "3u", "4u", "5u", "6u");
    }

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