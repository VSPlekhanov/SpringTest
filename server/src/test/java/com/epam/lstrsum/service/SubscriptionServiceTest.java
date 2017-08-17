package com.epam.lstrsum.service;

import com.epam.lstrsum.SetUpDataBaseCollections;
import com.epam.lstrsum.dto.question.QuestionPostDto;
import com.epam.lstrsum.model.Question;
import com.epam.lstrsum.model.Subscription;
import com.epam.lstrsum.persistence.QuestionRepository;
import com.epam.lstrsum.persistence.SubscriptionRepository;
import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.epam.lstrsum.testutils.InstantiateUtil.someString;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class SubscriptionServiceTest extends SetUpDataBaseCollections {

    @Autowired
    private SubscriptionService subscriptionService;
    @Autowired
    private QuestionService questionService;
    @Autowired
    private SubscriptionRepository subscriptionRepository;
    @Autowired
    private QuestionRepository questionRepository;

    @Test
    public void addSubscriptionAlreadyAdded() {
        String userId = "1u";
        String alreadySubscribed = "6u_6r";

        int previousSize = findAllQuestionWhichSubscribedByUserId(userId).size();

        subscriptionService.addOrUpdate(userId, questionRepository.findOne(alreadySubscribed).getQuestionId());
        int sizeAfterAdd = findAllQuestionWhichSubscribedByUserId(userId).size();

        assertEquals(previousSize, sizeAfterAdd);
    }

    @Test
    public void updateListOfSubscription() {
        String userWithSubscriptions = "6u";
        int previousSize = findAllQuestionWhichSubscribedByUserId(userWithSubscriptions).size();

        subscriptionService.addOrUpdate(userWithSubscriptions, questionRepository.findOne("1u_1r").getQuestionId());
        int actual = findAllQuestionWhichSubscribedByUserId(userWithSubscriptions).size();

        assertThat(previousSize + 1, is(actual));
    }

    @Test
    public void addWhenTryToUpdate() {
        String userWithoutSubscriptions = "7u";
        assertThat(findAllQuestionWhichSubscribedByUserId(userWithoutSubscriptions).size(), is(0));

        List<Question> allQuestion = questionRepository.findAll();
        subscriptionService.addOrUpdate(
                userWithoutSubscriptions,
                allQuestion.stream().map(Question::getQuestionId).collect(Collectors.toList())
        );

        assertThat(findAllQuestionWhichSubscribedByUserId(userWithoutSubscriptions).size(), is(allQuestion.size()));
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
        String questionId = "1u_1r";

        List<Subscription> subscriptions = subscriptionService.findAllSubscriptionsEntitiesToQuestionWithThisId(questionId);
        List<String> subscriptionIds = subscriptions.stream().map(Subscription::getSubscriptionId).collect(Collectors.toList());

        assertThat(subscriptionIds, containsInAnyOrder("2u_1s", "3u_1s"));
    }

    @Test
    public void subscriptionSearchByQuestionIdReturnsEmptyListIfThisQuestionHasNoSubscribers() throws Exception {
        String questionId = "questionWithNoSubscribersId";

        List<Subscription> emptyList = subscriptionService.findAllSubscriptionsEntitiesToQuestionWithThisId(questionId);

        assertThat(emptyList.isEmpty(), is(true));
    }

    @Test
    public void subscriptionSearchQueryReturnsOnlyIdAndUserIdFieldsAndDoesNotReturnListOfQuestionIds() {
        String questionId = "1u_1r";

        List<Subscription> subscriptions = subscriptionService.findAllSubscriptionsEntitiesToQuestionWithThisId(questionId);

        subscriptions.forEach(s -> assertThat(s.getSubscriptionId() == null, is(false)));
        subscriptions.forEach(s -> assertThat(s.getUserId() == null, is(false)));

        subscriptions.forEach(s -> assertThat(s.getQuestionIds() == null, is(true)));
    }

    @Test
    public void getEmailsForNotificationReturnsListOfEmailsWhereToSendNotificationAboutNewAnswerToThisQuestionIfItHasSubscribedUsers() throws
            Exception {
        String questionId = "1u_1r";

        List<String> emails = subscriptionService.getEmailsOfSubscribersOfQuestion(questionId);

        assertThat(emails, containsInAnyOrder("Bob_Hoplins@epam.com", "Tyler_Greeds@epam.com"));
    }

    @Test
    public void getEmailForNotificationReturnsEmptyListIfThisQuestionHasNoSubscribers() throws Exception {
        String questionId = "questionWithNoSubscribersId";

        List<String> emails = subscriptionService.getEmailsOfSubscribersOfQuestion(questionId);

        assertThat(emails.isEmpty(), is(true));
    }

    @Test
    public void getEmailsForNotificationReturnsListWithOneEmailOfAuthorIfThereIsNoAllowedSubs() throws Exception {
        QuestionPostDto postDto = new QuestionPostDto(someString(), new String[]{"1", "2", "3", "go"},
                "just some text", 1501144323239L,
                Collections.emptyList());
        String authorEmail = "John_Doe@epam.com";

        String newQuestionId = questionService.addNewQuestion(postDto, authorEmail).getQuestionId();

        List<String> emails = subscriptionService.getEmailsOfAuthorAndAllowedSubsOfQuestion(newQuestionId);

        MatcherAssert.assertThat(emails.size(), equalTo(1));
        MatcherAssert.assertThat(emails.get(0), equalTo(authorEmail));
    }

    @Test
    public void getEmailsForNotificationReturnsListWithEmailsOfAuthorAndAllowedSubsIfThereIsSome() throws Exception {
        QuestionPostDto postDto = new QuestionPostDto(someString(), new String[]{"1", "2", "3", "go"},
                "just some text", 1501144323239L,
                Arrays.asList("Bob_Hoplins@epam.com", "Tyler_Greeds@epam.com",
                        "Donald_Gardner@epam.com", "Ernest_Hemingway@epam.com"));
        String authorEmail = "John_Doe@epam.com";

        String newQuestionId = questionService.addNewQuestion(postDto, authorEmail).getQuestionId();

        List<String> emails = subscriptionService.getEmailsOfAuthorAndAllowedSubsOfQuestion(newQuestionId);

        assertThat(emails, containsInAnyOrder("Bob_Hoplins@epam.com", "Tyler_Greeds@epam.com",
                "Donald_Gardner@epam.com", "Ernest_Hemingway@epam.com", "John_Doe@epam.com"));
    }

    @Test
    public void getEmailsToNotificateAboutNewQuestionReturnsEmptyListIfThereIsNoAllowedSubs() throws Exception {
        QuestionPostDto postDto = new QuestionPostDto(someString(), new String[]{"1", "2", "3", "go"},
                "just some text", 1501144323239L,
                Collections.emptyList());
        String authorEmail = "John_Doe@epam.com";

        Question question = questionService.addNewQuestion(postDto, authorEmail);
        Set<String> emails = subscriptionService.getEmailsToNotificateAboutNewQuestion(question);

        MatcherAssert.assertThat(emails.isEmpty(), is(true));
    }

    @Test
    public void getEmailsToNotificateAboutNewQuestionReturnsListOfEmails() throws Exception {
        QuestionPostDto postDto = new QuestionPostDto(someString(), new String[]{"1", "2", "3", "go"},
                "just some text", 1501144323239L,
                Arrays.asList("Bob_Hoplins@epam.com", "Tyler_Greeds@epam.com",
                        "Donald_Gardner@epam.com", "Ernest_Hemingway@epam.com"));
        String authorEmail = "John_Doe@epam.com";

        Question question = questionService.addNewQuestion(postDto, authorEmail);
        Set<String> emails = subscriptionService.getEmailsToNotificateAboutNewQuestion(question);

        assertThat(emails, containsInAnyOrder("Bob_Hoplins@epam.com", "Tyler_Greeds@epam.com",
                "Donald_Gardner@epam.com", "Ernest_Hemingway@epam.com"));
    }

    @Test
    public void getEmailsToNotificateAboutNewAnswerReturnsSetWithEmails() {
        String questionId = "1u_1r";
        String user = "Bob_Hoplins@epam.com";

        Set<String> emails = subscriptionService.getEmailsToNotificateAboutNewAnswer(questionId);

        assertNotNull(emails);
        assertThat(emails.size(), is(5));
        assertThat(emails, hasItem(user));
    }
}