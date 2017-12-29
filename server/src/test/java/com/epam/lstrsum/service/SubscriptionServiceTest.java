package com.epam.lstrsum.service;

import com.epam.lstrsum.SetUpDataBaseCollections;
import com.epam.lstrsum.exception.BusinessLogicException;
import com.epam.lstrsum.exception.NoSuchUserException;
import com.epam.lstrsum.model.User;
import com.epam.lstrsum.persistence.QuestionRepository;
import com.epam.lstrsum.service.impl.SubscriptionServiceImpl;
import lombok.val;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.epam.lstrsum.testutils.InstantiateUtil.*;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SubscriptionServiceTest extends SetUpDataBaseCollections {

    private static List<String> allowedSubsList = Arrays.asList("bob_hoplins@epam.com", "tyler_greeds@epam.com",
            "donald_gardner@epam.com", "ernest_hemingway@epam.com");

    @Autowired
    private SubscriptionServiceImpl subscriptionService;
    @Autowired
    private QuestionService questionService;
    @Autowired
    private QuestionRepository questionRepository;
    @Autowired
    private UserService userService;


    @Test
    public void subscribeForQuestionByUserHasBeenAlreadySubscribedTest() {
        String questionId = "1u_1r";
        String userEmail = "Bob_Hoplins@epam.com";
        User user = userService.findUserByEmailOrThrowException(userEmail);
        List<User> listBeforeUpdate = questionService.getQuestionById(questionId).getSubscribers();
        assertTrue(subscriptionService.subscribeForQuestionByUser(questionId, userEmail));

        List<User> listAfterUpdate = questionService.getQuestionById(questionId).getSubscribers();
        assertThat(listAfterUpdate).hasSize(listBeforeUpdate.size());
        assertThat(listAfterUpdate.stream().anyMatch(u -> u.equals(user)));
    }


    @Test
    public void subscribeForQuestionByUserSuccessTest() {
        String questionId = "1u_1r";
        String userEmail = "John_Doe@epam.com";
        User user = userService.findUserByEmailOrThrowException(userEmail);
        List<User> listBeforeUpdate = questionService.getQuestionById(questionId).getSubscribers();
        assertTrue(subscriptionService.subscribeForQuestionByUser(questionId, userEmail));

        List<User> listAfterUpdate = questionService.getQuestionById(questionId).getSubscribers();
        assertThat(listAfterUpdate).hasSize(listBeforeUpdate.size() + 1);
        assertThat(listAfterUpdate.stream().anyMatch(u -> u.equals(user)));
    }

    @Test(expected = NoSuchUserException.class)
    public void subscribeForQuestionNotExistOrByUserNotExistTest() {
        assertFalse(subscriptionService.subscribeForQuestionByUser(NON_EXISTING_QUESTION_ID, SOME_USER_EMAIL));
        subscriptionService.subscribeForQuestionByUser(EXISTING_QUESTION_ID, "not_exist@epam.com");
    }

    @Test
    public void unsubscribeForQuestionByUserHasBeenAlreadyUnsubscribedTest() {
        String questionId = "1u_1r";
        String userEmail = "John_Doe@epam.com";
        User user = userService.findUserByEmailOrThrowException(userEmail);
        List<User> listBeforeUpdate = questionService.getQuestionById(questionId).getSubscribers();
        assertTrue(subscriptionService.unsubscribeForQuestionByUser(questionId, userEmail));

        List<User> listAfterUpdate = questionService.getQuestionById(questionId).getSubscribers();
        assertThat(listAfterUpdate).hasSize(listBeforeUpdate.size());
        assertThat(not(listAfterUpdate.stream().anyMatch(u -> u.equals(user))));
    }

    @Test
    public void unsubscribeForQuestionByUserSuccessTest() {
        String questionId = "1u_1r";
        String userEmail = "Bob_Hoplins@epam.com";
        User user = userService.findUserByEmailOrThrowException(userEmail);
        List<User> listBeforeUpdate = questionService.getQuestionById(questionId).getSubscribers();
        assertTrue(subscriptionService.unsubscribeForQuestionByUser(questionId, userEmail));

        List<User> listAfterUpdate = questionService.getQuestionById(questionId).getSubscribers();
        assertThat(listAfterUpdate).hasSize(listBeforeUpdate.size() - 1);
        assertThat(not(listAfterUpdate.stream().anyMatch(u -> u.equals(user))));
    }

    @Test(expected = NoSuchUserException.class)
    public void unsubscribeForQuestionNotExistOrByUserNotExistTest() {
        assertFalse(subscriptionService.unsubscribeForQuestionByUser(NON_EXISTING_QUESTION_ID, SOME_USER_EMAIL));
        subscriptionService.unsubscribeForQuestionByUser(EXISTING_QUESTION_ID, "not_exist@epam.com");
    }

    @Test(expected = BusinessLogicException.class)
    public void checkQuestionExistsTest() {
        ((SubscriptionServiceImpl)subscriptionService)
                .checkQuestionExistsAndUserHasPermission(NON_EXISTING_QUESTION_ID, someString());
    }

    @Test(expected = BusinessLogicException.class)
    public void checkUserHasPermissionTest() {
        ((SubscriptionServiceImpl)subscriptionService)
                .checkQuestionExistsAndUserHasPermission("6u_6r", "Donald_Gardner@epam.com");
    }

    @Test
    public void checkQuestionExistsAndUserHasPermissionTest() {
        ((SubscriptionServiceImpl)subscriptionService)
                .checkQuestionExistsAndUserHasPermission("6u_6r", "tyler_greeds@epam.com");
    }

    @Test
    public void subscribeForQuestionByAllowedSubSuccessTest() {
        String questionId = "6u_6r";
        String userEmail = "bob_hoplins@epam.com";
        User user = userService.findUserByEmailOrThrowException(userEmail);
        List<User> listBeforeUpdate = questionService.getQuestionById(questionId).getSubscribers();
        assertTrue(subscriptionService.subscribeForQuestionByAllowedSub(questionId, userEmail));

        List<User> listAfterUpdate = questionService.getQuestionById(questionId).getSubscribers();
        assertThat(listAfterUpdate).hasSize(listBeforeUpdate.size() + 1);
        assertThat(listAfterUpdate.stream().anyMatch(u -> u.equals(user)));
    }

    @Test
    public void unsubscribeForQuestionByAllowedSubSuccessTest() {
        String questionId = "6u_6r";
        String userEmail = "tyler_greeds@epam.com";
        User user = userService.findUserByEmailOrThrowException(userEmail);
        List<User> listBeforeUpdate = questionService.getQuestionById(questionId).getSubscribers();
        assertTrue(subscriptionService.unsubscribeForQuestionByAllowedSub(questionId, userEmail));

        List<User> listAfterUpdate = questionService.getQuestionById(questionId).getSubscribers();
        assertThat(listAfterUpdate).hasSize(listBeforeUpdate.size() - 1);
        assertThat(not(listAfterUpdate.stream().anyMatch(u -> u.equals(user))));
    }

    @Test
    public void getEmailsToNotifyAboutNewQuestionFromPortalReturnsEmptyListIfThereIsNoAllowedSubs() throws Exception {
        val postDto = someQuestionPostDtoWithAllowedSubs(emptyList());

        val question = questionService.addNewQuestion(postDto, SOME_USER_EMAIL);
        val emails = subscriptionService.getEmailsToNotifyAboutNewQuestionFromPortal(question);

        assertThat(emails).hasSize(userService.findAllActive().size());
    }

    @Test
    public void getEmailsToNotifyAboutNewQuestionFromPortalReturnsListOfEmails() throws Exception {
        val postDto = someQuestionPostDtoWithAllowedSubs(allowedSubsList);

        val question = questionService.addNewQuestion(postDto, SOME_USER_EMAIL);
        val emails = subscriptionService.getEmailsToNotifyAboutNewQuestionFromPortal(question);

        assertThat(emails).containsExactlyInAnyOrder("bob_hoplins@epam.com", "tyler_greeds@epam.com",
                "donald_gardner@epam.com", "ernest_hemingway@epam.com", "john_doe@epam.com", "tyler_derden@mylo.com");
    }

    @Test
    public void getEmailsToNotificateAboutNewAnswerReturnsSetWithEmails() {
        val emails = subscriptionService.getEmailsToNotifyAboutNewAnswerFromPortal("6u_6r");

        assertThat(emails).hasSize(2);
        assertThat(emails).containsExactlyInAnyOrder("john_doe@epam.com", "tyler_greeds@epam.com");
    }

    @Test
    public void getEmailsToNotifyAllowedSubsAndAuthorAndDLAboutNewQuestionFromEmail() {
        val postDto = someQuestionPostDtoWithAllowedSubs(allowedSubsList);
        subscriptionService.setNotifyAllowedSubs(true);
        subscriptionService.setNotifyDL(true);

        val question = questionService.addNewQuestion(postDto, SOME_USER_EMAIL);
        val emails = subscriptionService.getEmailsToNotifyAboutNewQuestionFromEmail(question);
        val dLEmails = userService.findAllActive().stream().map(User::getEmail).collect(Collectors.toList());
        dLEmails.addAll(allowedSubsList);
        assertThat(emails).containsExactlyInAnyOrder(dLEmails.stream().distinct().toArray(String[]::new));
    }

    @Test
    public void getEmailsToNotifyAllowedSubsAndAuthorAboutNewQuestionFromEmail() {
        val postDto = someQuestionPostDtoWithAllowedSubs(allowedSubsList);
        subscriptionService.setNotifyAllowedSubs(true);
        subscriptionService.setNotifyDL(false);

        val question = questionService.addNewQuestion(postDto, SOME_USER_EMAIL);
        val emails = subscriptionService.getEmailsToNotifyAboutNewQuestionFromEmail(question);
        List<String> expectedEmails = new ArrayList<>();
        expectedEmails.addAll(allowedSubsList);
        assertThat(emails).containsExactlyInAnyOrder(expectedEmails.stream().distinct().toArray(String[]::new));
    }

    @Test
    public void getEmailsToNotifyAllowedSubsAndDlAboutNewQuestionFromEmail() {
        val postDto = someQuestionPostDtoWithAllowedSubs(allowedSubsList);
        subscriptionService.setNotifyAllowedSubs(true);
        subscriptionService.setNotifyDL(true);

        val question = questionService.addNewQuestion(postDto, SOME_USER_EMAIL);
        val emails = subscriptionService.getEmailsToNotifyAboutNewQuestionFromEmail(question);
        val dLEmails = userService.findAllActive().stream().map(User::getEmail).collect(Collectors.toList());
        dLEmails.addAll(allowedSubsList);
        assertThat(emails).containsExactlyInAnyOrder(dLEmails.stream().distinct().toArray(String[]::new));
    }

    @Test
    public void getEmailsToNotifyAllowedSubsAboutNewQuestionFromEmail() {
        val postDto = someQuestionPostDtoWithAllowedSubs(allowedSubsList);
        subscriptionService.setNotifyAllowedSubs(true);
        subscriptionService.setNotifyDL(false);

        val question = questionService.addNewQuestion(postDto, SOME_USER_EMAIL);
        val emails = subscriptionService.getEmailsToNotifyAboutNewQuestionFromEmail(question);
        assertThat(emails).containsExactlyInAnyOrder(allowedSubsList.toArray(new String[allowedSubsList.size()]));
    }

    @Test
    public void getEmailsToNotifyNobodyAboutNewQuestionFromEmail() {
        val postDto = someQuestionPostDtoWithAllowedSubs(allowedSubsList);
        subscriptionService.setNotifyAllowedSubs(false);
        subscriptionService.setNotifyDL(false);

        val question = questionService.addNewQuestion(postDto, SOME_USER_EMAIL);
        val emails = subscriptionService.getEmailsToNotifyAboutNewQuestionFromEmail(question);
        assertTrue(emails.isEmpty());
    }

}