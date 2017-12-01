package com.epam.lstrsum.service.impl;

import com.epam.lstrsum.dto.answer.AnswerAllFieldsDto;
import com.epam.lstrsum.email.EmailCollection;
import com.epam.lstrsum.exception.BusinessLogicException;
import com.epam.lstrsum.model.Question;
import com.epam.lstrsum.model.User;
import com.epam.lstrsum.service.QuestionService;
import com.epam.lstrsum.service.SubscriptionService;
import com.epam.lstrsum.service.UserService;
import com.mongodb.DBRef;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.mail.Address;
import javax.mail.internet.InternetAddress;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.isNull;

@Service
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "notify")
@Slf4j
public class SubscriptionServiceImpl implements SubscriptionService {
    private final QuestionService questionService;
    private final UserService userService;
    private final MongoTemplate mongoTemplate;

    @Value("${spring.mail.username}")
    private String fromAddress;

    @Setter
    private boolean notifyAllowedSubs;
    @Setter
    private boolean notifyAuthor;
    @Setter
    private boolean notifyDL;

    private static Address[] getAddressesFromEmails(Collection<String> emails) {
        return emails.stream()
                .map((s) -> {
                    try {
                        return new InternetAddress(s);
                    } catch (Exception e) {
                        log.warn("Could not parse email address: {} {}", s, e.getMessage());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toArray(Address[]::new);
    }

    @Override
    public Set<String> getEmailsToNotifyAboutNewQuestionFromPortal(Question question) {
        return Stream.concat(question.getAllowedSubs().stream(), userService.findAllActive().stream())
                .map(User::getEmail)
                .filter(e -> !e.equalsIgnoreCase(fromAddress))
                .collect(Collectors.toSet());
    }

    @Override
    public Set<String> getEmailsToNotifyAboutNewAnswerFromPortal(String questionId) {
        return questionService.getQuestionById(questionId).getSubscribers().stream()
                .map(User::getEmail)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<String> getEmailsToNotifyAboutNewQuestionFromEmail(Question question) {
        return Stream.concat(Stream.concat(
                notifyAllowedSubs ? question.getAllowedSubs().stream() : Stream.empty(),
                notifyDL ? userService.findAllActive().stream() : Stream.empty()
            ), notifyAuthor ? Stream.of(question.getAuthorId()) : Stream.empty())
            .map(User::getEmail)
            .filter(e -> !e.equalsIgnoreCase(fromAddress))
            .collect(Collectors.toSet());
    }

    @Override
    public Set<String> getEmailsToNotifyAboutNewAnswerFromEmail(String questionId) {
        throw new UnsupportedOperationException();
    }

    public void checkQuestionExistsAndUserHasPermission(String questionId, String userEmail) {
        Question question = mongoTemplate.findOne(getQueryForQuestionId(questionId), Question.class);
        if (isNull(question) || !isUserAllowedSubOnQuestion(question, userEmail)) {
            throw new BusinessLogicException(
                    "Question doesn't exist or user with email : '" + userEmail + " ' has no permission to answer id : '" + questionId);
        }
    }

    private boolean isUserAllowedSubOnQuestion(Question question, String userEmail) {
        return question.getAllowedSubs().stream().map(User::getEmail).anyMatch(email -> email.equals(userEmail));
    }

    @Override
    public boolean subscribeForQuestionByUser(String questionId, String email) {
        String userId = userService.findUserByEmailOrThrowException(email).getUserId();
        Update addSubscription = new Update().addToSet("subscribers")
                .value(new DBRef(User.USER_COLLECTION_NAME, userId));
        return updateQuestionWithSubscriber(getQueryForQuestionId(questionId), addSubscription);

    }

    @Override
    public boolean unsubscribeForQuestionByUser(String questionId, String email) {
        String userId = userService.findUserByEmailOrThrowException(email).getUserId();
        Update pullSubscription = new Update().pull("subscribers",
                new DBRef(User.USER_COLLECTION_NAME, userId));
        return updateQuestionWithSubscriber(getQueryForQuestionId(questionId), pullSubscription);

    }

    @Override
    public boolean subscribeForQuestionByAllowedSub(String questionId, String email) {
        checkQuestionExistsAndUserHasPermission(questionId, email);
        return subscribeForQuestionByUser(questionId, email);
    }

    @Override
    public boolean unsubscribeForQuestionByAllowedSub(String questionId, String email) {
        checkQuestionExistsAndUserHasPermission(questionId, email);
        return unsubscribeForQuestionByUser(questionId, email);
    }

    private Query getQueryForQuestionId(String questionId) {
        return new Query(Criteria.where("_id").is(questionId));
    }

    private boolean updateQuestionWithSubscriber(Query query, Update update) {
        return mongoTemplate.updateFirst(query, update, Question.class).isUpdateOfExisting();
    }

    @Component
    @RequiredArgsConstructor
    public static class QuestionEmailCollectionAdapter implements EmailCollection<Question> {
        private final SubscriptionService subscriptionService;

        @Override
        public Address[] getEmailAddressesToNotifyFromEmail(Question question) {
            return getAddressesFromEmails(
                    new HashSet<>(subscriptionService.getEmailsToNotifyAboutNewQuestionFromEmail(question)));
        }

        @Override
        public Address[] getEmailAddressesToNotifyFromPortal(Question question) {
            return getAddressesFromEmails(
                    new HashSet<>(subscriptionService.getEmailsToNotifyAboutNewQuestionFromPortal(question)));
        }
    }

    @Component
    @RequiredArgsConstructor
    public static class AnswerEmailCollectionAdapter implements EmailCollection<AnswerAllFieldsDto> {
        private final SubscriptionService subscriptionService;

        @Override
        public Address[] getEmailAddressesToNotifyFromPortal(AnswerAllFieldsDto answer) {
            return getAddressesFromEmails(
                    new HashSet<>(subscriptionService.getEmailsToNotifyAboutNewAnswerFromPortal(
                            answer.getQuestion().getQuestionId()
                    )));
        }

        @Override
        public Address[] getEmailAddressesToNotifyFromEmail(AnswerAllFieldsDto answer) {
            return getAddressesFromEmails(
                    new HashSet<>(subscriptionService.getEmailsToNotifyAboutNewAnswerFromEmail(
                            answer.getQuestion().getQuestionId()
                    )));
        }
    }
}
