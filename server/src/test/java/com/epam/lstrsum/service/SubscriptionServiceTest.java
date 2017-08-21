package com.epam.lstrsum.service;

import com.epam.lstrsum.SetUpDataBaseCollections;
import com.epam.lstrsum.dto.question.QuestionPostDto;
import com.epam.lstrsum.model.Question;
import com.epam.lstrsum.model.Subscription;
import com.epam.lstrsum.persistence.QuestionRepository;
import com.epam.lstrsum.persistence.SubscriptionRepository;
import lombok.val;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.epam.lstrsum.testutils.InstantiateUtil.someString;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class SubscriptionServiceTest extends SetUpDataBaseCollections {

    @Autowired
    private SubscriptionService subscriptionService;
    @Autowired
    private QuestionService questionService;
    @Autowired
    private SubscriptionRepository subscriptionRepository;
    @Autowired
    private QuestionRepository questionRepository;
    @Autowired
    private UserService userService;

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

        assertEquals(previousSize + 1, actual);
    }

    @Test
    public void addWhenTryToUpdate() {
        String userWithoutSubscriptions = "7u";
        assertThat(findAllQuestionWhichSubscribedByUserId(userWithoutSubscriptions)).hasSize(0);

        List<Question> allQuestion = questionRepository.findAll();
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
        String questionId = "1u_1r";

        List<Subscription> subscriptions = subscriptionService.findAllSubscriptionsEntitiesToQuestionWithThisId(questionId);
        List<String> subscriptionIds = subscriptions.stream().map(Subscription::getSubscriptionId).collect(Collectors.toList());

        assertThat(subscriptionIds).containsExactlyInAnyOrder("2u_1s", "3u_1s");
    }

    @Test
    public void subscriptionSearchByQuestionIdReturnsEmptyListIfThisQuestionHasNoSubscribers() throws Exception {
        String questionId = "questionWithNoSubscribersId";

        List<Subscription> emptyList = subscriptionService.findAllSubscriptionsEntitiesToQuestionWithThisId(questionId);

        assertThat(emptyList).isEmpty();
    }

    @Test
    public void subscriptionSearchQueryReturnsOnlyIdAndUserIdFieldsAndDoesNotReturnListOfQuestionIds() {
        String questionId = "1u_1r";

        List<Subscription> subscriptions = subscriptionService.findAllSubscriptionsEntitiesToQuestionWithThisId(questionId);

        val subscriptionIds = Arrays.asList("3u_1s", "2u_1s");
        val userIds = Arrays.asList("2u", "3u");

        subscriptions.forEach(s -> assertThat(subscriptionIds).contains(s.getSubscriptionId()));
        subscriptions.forEach(s -> assertThat(userIds).contains(s.getUserId().getUserId()));
    }

    @Test
    public void getEmailsForNotificationReturnsListOfEmailsWhereToSendNotificationAboutNewAnswerToThisQuestionIfItHasSubscribedUsers() throws
            Exception {
        String questionId = "1u_1r";

        List<String> emails = subscriptionService.getEmailsOfSubscribersOfQuestion(questionId);

        assertThat(emails).containsExactlyInAnyOrder("Bob_Hoplins@epam.com", "Tyler_Greeds@epam.com");
    }

    @Test
    public void getEmailForNotificationReturnsEmptyListIfThisQuestionHasNoSubscribers() throws Exception {
        String questionId = "questionWithNoSubscribersId";

        List<String> emails = subscriptionService.getEmailsOfSubscribersOfQuestion(questionId);

        assertThat(emails).isEmpty();
    }

    @Test
    public void getEmailsForNotificationReturnsListWithOneEmailOfAuthorIfThereIsNoAllowedSubs() throws Exception {
        QuestionPostDto postDto = new QuestionPostDto(someString(), new String[]{"1", "2", "3", "go"},
                "just some text", 1501144323239L,
                emptyList(), emptyList());
        String authorEmail = "John_Doe@epam.com";

        String newQuestionId = questionService.addNewQuestion(postDto, authorEmail).getQuestionId();

        List<String> emails = subscriptionService.getEmailsOfAuthorAndAllowedSubsOfQuestion(newQuestionId);

        assertThat(emails)
                .hasSize(6)
                .contains(authorEmail);
    }

    @Test
    public void getEmailsForNotificationReturnsListWithEmailsOfAuthorAndAllowedSubsIfThereIsSome() throws Exception {
        QuestionPostDto postDto = new QuestionPostDto(someString(), new String[]{"1", "2", "3", "go"},
                "just some text", 1501144323239L,
                Arrays.asList("Bob_Hoplins@epam.com", "Tyler_Greeds@epam.com",
                        "Donald_Gardner@epam.com", "Ernest_Hemingway@epam.com"), emptyList());
        String authorEmail = "John_Doe@epam.com";

        String newQuestionId = questionService.addNewQuestion(postDto, authorEmail).getQuestionId();

        List<String> emails = subscriptionService.getEmailsOfAuthorAndAllowedSubsOfQuestion(newQuestionId);

        assertThat(emails).containsExactlyInAnyOrder("Bob_Hoplins@epam.com", "Tyler_Greeds@epam.com",
                "Donald_Gardner@epam.com", "Ernest_Hemingway@epam.com", "John_Doe@epam.com",
                "Bob_Hoplins@epam.com", "Donald_Gardner@epam.com", "Ernest_Hemingway@epam.com", "Tyler_Derden@mylo.com",
                "John_Doe@epam.com");
    }

    @Test
    public void getEmailsToNotificateAboutNewQuestionReturnsEmptyListIfThereIsNoAllowedSubs() throws Exception {
        QuestionPostDto postDto = new QuestionPostDto(someString(), new String[]{"1", "2", "3", "go"},
                "just some text", 1501144323239L,
                emptyList(), emptyList());
        String authorEmail = "John_Doe@epam.com";

        Question question = questionService.addNewQuestion(postDto, authorEmail);
        Set<String> emails = subscriptionService.getEmailsToNotificateAboutNewQuestion(question);

        assertThat(emails).hasSize(userService.findAllActive().size());
    }

    @Test
    public void getEmailsToNotificateAboutNewQuestionReturnsListOfEmails() throws Exception {
        QuestionPostDto postDto = new QuestionPostDto(someString(), new String[]{"1", "2", "3", "go"},
                "just some text", 1501144323239L,
                Arrays.asList("Bob_Hoplins@epam.com", "Tyler_Greeds@epam.com",
                        "Donald_Gardner@epam.com", "Ernest_Hemingway@epam.com"), emptyList());
        String authorEmail = "John_Doe@epam.com";

        Question question = questionService.addNewQuestion(postDto, authorEmail);
        Set<String> emails = subscriptionService.getEmailsToNotificateAboutNewQuestion(question);

        assertThat(emails).containsExactlyInAnyOrder("Bob_Hoplins@epam.com", "Tyler_Greeds@epam.com",
                "Donald_Gardner@epam.com", "Ernest_Hemingway@epam.com", "John_Doe@epam.com", "Tyler_Derden@mylo.com");
    }

    @Test
    public void getEmailsToNotificateAboutNewAnswerReturnsSetWithEmails() {
        String questionId = "1u_1r";
        String user = "Bob_Hoplins@epam.com";

        Set<String> emails = subscriptionService.getEmailsToNotificateAboutNewAnswer(questionId);

        assertNotNull(emails);
        assertThat(emails).hasSize(7);
        assertThat(emails).contains(user);
    }
}