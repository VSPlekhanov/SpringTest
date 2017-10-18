package com.epam.lstrsum.service;

import com.epam.lstrsum.SetUpDataBaseCollections;
import com.epam.lstrsum.model.Question;
import com.epam.lstrsum.model.Subscription;
import com.epam.lstrsum.persistence.QuestionRepository;
import lombok.val;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.epam.lstrsum.testutils.InstantiateUtil.EXISTING_QUESTION_ID;
import static com.epam.lstrsum.testutils.InstantiateUtil.EXISTING_USER_ID;
import static com.epam.lstrsum.testutils.InstantiateUtil.SOME_USER_EMAIL;
import static com.epam.lstrsum.testutils.InstantiateUtil.someQuestionPostDtoWithAllowedSubs;
import static com.epam.lstrsum.testutils.InstantiateUtil.someString;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class SubscriptionServiceTest extends SetUpDataBaseCollections {

    private static List<String> allowedSubsList = Arrays.asList("Bob_Hoplins@epam.com", "Tyler_Greeds@epam.com",
            "Donald_Gardner@epam.com", "Ernest_Hemingway@epam.com");

    @Autowired
    private SubscriptionService subscriptionService;
    @Autowired
    private QuestionService questionService;
    @Autowired
    private QuestionRepository questionRepository;
    @Autowired
    private UserService userService;

    @Test
    public void addSubscriptionAlreadyAdded() {
        val alreadySubscribed = "6u_6r";
        val previousSize = findAllQuestionWhichSubscribedByUserId(EXISTING_USER_ID).size();

        subscriptionService.addOrUpdate(EXISTING_USER_ID, questionRepository.findOne(alreadySubscribed).getQuestionId());
        val sizeAfterAdd = findAllQuestionWhichSubscribedByUserId(EXISTING_USER_ID).size();

        assertEquals(previousSize, sizeAfterAdd);
    }

    @Test
    public void updateListOfSubscription() {
        val userWithSubscriptions = "6u";
        int previousSize = findAllQuestionWhichSubscribedByUserId(userWithSubscriptions).size();

        subscriptionService.addOrUpdate(userWithSubscriptions, questionRepository.findOne(EXISTING_QUESTION_ID).getQuestionId());
        val actual = findAllQuestionWhichSubscribedByUserId(userWithSubscriptions).size();

        assertEquals(previousSize + 1, actual);
    }

    @Test
    public void addWhenTryToUpdate() {
        val userWithoutSubscriptions = "7u";
        assertThat(findAllQuestionWhichSubscribedByUserId(userWithoutSubscriptions)).hasSize(0);

        val allQuestion = questionRepository.findAll();
        subscriptionService.addOrUpdate(
                userWithoutSubscriptions,
                allQuestion.stream().map(Question::getQuestionId).collect(Collectors.toList())
        );

        assertThat(findAllQuestionWhichSubscribedByUserId(userWithoutSubscriptions)).hasSize(allQuestion.size());
    }

    private List<Question> findAllQuestionWhichSubscribedByUserId(String id) {
        return subscriptionService.findAll()
                .stream()
                .filter(u -> u.getUserId().getUserId().equals(id))
                .flatMap(s -> s.getQuestionIds().stream())
                .collect(Collectors.toList());
    }

    @Test
    public void subscriptionSearchByQuestionIdReturnsListOfMatchedSubscriptions() throws Exception {
        val subscriptions = subscriptionService.findAllSubscriptionsEntitiesToQuestionWithThisId(EXISTING_QUESTION_ID);
        val subscriptionIds = subscriptions.stream().map(Subscription::getSubscriptionId).collect(Collectors.toList());

        assertThat(subscriptionIds).containsExactlyInAnyOrder("2u_1s", "3u_1s");
    }

    @Test
    public void subscriptionSearchByQuestionIdReturnsEmptyListIfThisQuestionHasNoSubscribers() throws Exception {
        val emptyList = subscriptionService.findAllSubscriptionsEntitiesToQuestionWithThisId(someString());

        assertThat(emptyList).isEmpty();
    }

    @Test
    public void subscriptionSearchQueryReturnsOnlyIdAndUserIdFieldsAndDoesNotReturnListOfQuestionIds() {
        val subscriptions = subscriptionService.findAllSubscriptionsEntitiesToQuestionWithThisId(EXISTING_QUESTION_ID);

        val subscriptionIds = Arrays.asList("3u_1s", "2u_1s");
        val userIds = Arrays.asList("2u", "3u");

        subscriptions.forEach(s -> assertThat(subscriptionIds).contains(s.getSubscriptionId()));
        subscriptions.forEach(s -> assertThat(userIds).contains(s.getUserId().getUserId()));
    }

    @Test
    public void getEmailsForNotificationReturnsListOfEmailsWhereToSendNotificationAboutNewAnswerToThisQuestionIfItHasSubscribedUsers() throws
            Exception {
        val emails = subscriptionService.getEmailsOfSubscribersOfQuestion(EXISTING_QUESTION_ID);

        assertThat(emails).containsExactlyInAnyOrder("Bob_Hoplins@epam.com", "Tyler_Greeds@epam.com");
    }

    @Test
    public void getEmailForNotificationReturnsEmptyListIfThisQuestionHasNoSubscribers() throws Exception {
        val emails = subscriptionService.getEmailsOfSubscribersOfQuestion(someString());

        assertThat(emails).isEmpty();
    }

    @Test
    public void getEmailsForNotificationReturnsListWithOneEmailOfAuthorIfThereIsNoAllowedSubs() throws Exception {
        val postDto = someQuestionPostDtoWithAllowedSubs(emptyList());
        val authorEmail = SOME_USER_EMAIL;

        val newQuestionId = questionService.addNewQuestion(postDto, authorEmail).getQuestionId();

        val emails = subscriptionService.getEmailsOfAuthorAndAllowedSubsOfQuestion(newQuestionId);

        assertThat(emails)
                .hasSize(5)
                .contains(authorEmail);
    }

    @Test
    public void getEmailsForNotificationReturnsListWithEmailsOfAuthorAndAllowedSubsIfThereIsSome() throws Exception {
        val postDto = someQuestionPostDtoWithAllowedSubs(allowedSubsList);

        val newQuestionId = questionService.addNewQuestion(postDto, SOME_USER_EMAIL).getQuestionId();
        val emails = subscriptionService.getEmailsOfAuthorAndAllowedSubsOfQuestion(newQuestionId);

        assertThat(emails).containsExactlyInAnyOrder("Bob_Hoplins@epam.com", "Tyler_Greeds@epam.com",
                "Donald_Gardner@epam.com", "Ernest_Hemingway@epam.com", "John_Doe@epam.com",
                "Bob_Hoplins@epam.com", "Donald_Gardner@epam.com", "Tyler_Derden@mylo.com",
                "John_Doe@epam.com");
    }

    @Test
    public void getEmailsToNotificateAboutNewQuestionReturnsEmptyListIfThereIsNoAllowedSubs() throws Exception {
        val postDto = someQuestionPostDtoWithAllowedSubs(emptyList());

        val question = questionService.addNewQuestion(postDto, SOME_USER_EMAIL);
        val emails = subscriptionService.getEmailsToNotificateAboutNewQuestion(question);

        assertThat(emails).hasSize(userService.findAllActive().size());
    }

    @Test
    public void getEmailsToNotificateAboutNewQuestionReturnsListOfEmails() throws Exception {
        val postDto = someQuestionPostDtoWithAllowedSubs(allowedSubsList);

        val question = questionService.addNewQuestion(postDto, SOME_USER_EMAIL);
        val emails = subscriptionService.getEmailsToNotificateAboutNewQuestion(question);

        assertThat(emails).containsExactlyInAnyOrder("Bob_Hoplins@epam.com", "Tyler_Greeds@epam.com",
                "Donald_Gardner@epam.com", "Ernest_Hemingway@epam.com", "John_Doe@epam.com", "Tyler_Derden@mylo.com");
    }

    @Test
    public void getEmailsToNotificateAboutNewAnswerReturnsSetWithEmails() {
        val user = "Bob_Hoplins@epam.com";

        val emails = subscriptionService.getEmailsToNotificateAboutNewAnswer(EXISTING_QUESTION_ID);

        assertNotNull(emails);
        assertThat(emails).hasSize(7);
        assertThat(emails).contains(user);
    }

}