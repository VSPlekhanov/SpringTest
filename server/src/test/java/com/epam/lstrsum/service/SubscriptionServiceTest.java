package com.epam.lstrsum.service;

import com.epam.lstrsum.SetUpDataBaseCollections;
import com.epam.lstrsum.dto.request.RequestPostDto;
import com.epam.lstrsum.model.Subscription;
import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class SubscriptionServiceTest extends SetUpDataBaseCollections {

    @Autowired
    private SubscriptionService subscriptionService;
    @Autowired
    private RequestService requestService;

    @Test
    public void subscriptionSearchByRequestIdReturnsListOfMatchedSubscriptions() throws Exception {
        String requestId = "1u_1r";

        List<Subscription> subscriptions = subscriptionService.findAllSubscriptionsEntitiesToRequestWithThisId(requestId);
        List<String> subscriptionIds = subscriptions.stream().map(Subscription::getSubscriptionId).collect(Collectors.toList());

        assertThat(subscriptionIds, containsInAnyOrder("2u_1s", "3u_1s"));
    }

    @Test
    public void subscriptionSearchByRequestIdReturnsEmptyListIfThisRequestHasNoSubscribers() throws Exception {
        String requestId = "requestWithNoSubscribersId";

        List<Subscription> emptyList = subscriptionService.findAllSubscriptionsEntitiesToRequestWithThisId(requestId);

        assertThat(emptyList.isEmpty(), is(true));
    }

    @Test
    public void subscriptionSearchQueryReturnsOnlyIdAndUserIdFieldsAndDoesNotReturnListOfRequestIds() {
        String requsetId = "1u_1r";

        List<Subscription> subscriptions = subscriptionService.findAllSubscriptionsEntitiesToRequestWithThisId(requsetId);

        subscriptions.forEach(s -> assertThat(s.getSubscriptionId() == null, is(false)));
        subscriptions.forEach(s -> assertThat(s.getUserId() == null, is(false)));

        subscriptions.forEach(s -> assertThat(s.getRequestIds() == null, is(true)));
    }

    @Test
    public void getEmailsForNotificationReturnsListOfEmailsWhereToSendNotificationAboutNewAnswerToThisRequestIfItHasSubscribedUsers() throws Exception {
        String requestId = "1u_1r";

        List<String> emails = subscriptionService.getEmailsOfSubscribersOfRequest(requestId);

        assertThat(emails, containsInAnyOrder("Bob_Hoplins@epam.com", "Tyler_Greeds@epam.com"));
    }

    @Test
    public void getEmailForNotificationReturnsEmptyListIfThisRequestHasNoSubscribers() throws Exception {
        String requestId = "requestWithNoSubscribersId";

        List<String> emails = subscriptionService.getEmailsOfSubscribersOfRequest(requestId);

        assertThat(emails.isEmpty(), is(true));
    }

    @Test
    public void getEmailsForNotificationReturnsListWithOneEmailOfAuthorIfThereIsNoAllowedSubs() throws Exception {
        RequestPostDto postDto = new RequestPostDto("this the end", new String[]{"1", "2", "3", "go"},
                "just some text", 1501144323239L,
                Collections.emptyList());
        String authorEmail = "John_Doe@epam.com";

        String newRequestId = requestService.addNewRequest(postDto, authorEmail).getRequestId();

        List<String> emails = subscriptionService.getEmailsOfAuthorAndAllowedSubsOfRequest(newRequestId);

        MatcherAssert.assertThat(emails.size(), equalTo(1));
        MatcherAssert.assertThat(emails.get(0), equalTo(authorEmail));
    }

    @Test
    public void getEmailsForNotificationReturnsListWithEmailsOfAuthorAndAllowedSubsIfThereIsSome() throws Exception {
        RequestPostDto postDto = new RequestPostDto("this the end", new String[]{"1", "2", "3", "go"},
                "just some text", 1501144323239L,
                Arrays.asList("Bob_Hoplins@epam.com", "Tyler_Greeds@epam.com",
                        "Donald_Gardner@epam.com", "Ernest_Hemingway@epam.com"));
        String authorEmail = "John_Doe@epam.com";

        String newRequestId = requestService.addNewRequest(postDto, authorEmail).getRequestId();

        List<String> emails = subscriptionService.getEmailsOfAuthorAndAllowedSubsOfRequest(newRequestId);

        MatcherAssert.assertThat(emails, containsInAnyOrder("Bob_Hoplins@epam.com", "Tyler_Greeds@epam.com",
                "Donald_Gardner@epam.com", "Ernest_Hemingway@epam.com", "John_Doe@epam.com"));
    }

    @Test
    public void getEmailsToNotificateAboutNewRequestReturnsEmptyListIfThereIsNoAllowedSubs() throws Exception {
        RequestPostDto postDto = new RequestPostDto("this the end", new String[]{"1", "2", "3", "go"},
                "just some text", 1501144323239L,
                Collections.emptyList());
        String authorEmail = "John_Doe@epam.com";

        String newRequestId = requestService.addNewRequest(postDto, authorEmail).getRequestId();

        List<String> emails = subscriptionService.getEmailsToNotificateAboutNewRequest(newRequestId);

        MatcherAssert.assertThat(emails.isEmpty(), is(true));
    }

    @Test
    public void getEmailsToNotificateAboutNewRequestReturnsListOfEmails() throws Exception {
        RequestPostDto postDto = new RequestPostDto("this the end", new String[]{"1", "2", "3", "go"},
                "just some text", 1501144323239L,
                Arrays.asList("Bob_Hoplins@epam.com", "Tyler_Greeds@epam.com",
                        "Donald_Gardner@epam.com", "Ernest_Hemingway@epam.com"));
        String authorEmail = "John_Doe@epam.com";

        String newRequestId = requestService.addNewRequest(postDto, authorEmail).getRequestId();

        List<String> emails = subscriptionService.getEmailsToNotificateAboutNewRequest(newRequestId);

        MatcherAssert.assertThat(emails, containsInAnyOrder("Bob_Hoplins@epam.com", "Tyler_Greeds@epam.com",
                "Donald_Gardner@epam.com", "Ernest_Hemingway@epam.com"));
    }

    @Test
    public void getEmailsToNotificateAboutNewAnswerReturnsSetWithEmails() {
        String requestId = "1u_1r";
        String user = "Bob_Hoplins@epam.com";

        Set<String> emails = subscriptionService.getEmailsToNotificateAboutNewAnswer(requestId);

        assertNotNull(emails);
        assertThat(emails.size(), is(5));
        assertThat(emails, hasItem(user));
    }
}