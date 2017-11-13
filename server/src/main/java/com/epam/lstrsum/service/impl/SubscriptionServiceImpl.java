package com.epam.lstrsum.service.impl;

import com.epam.lstrsum.aggregators.SubscriptionAggregator;
import com.epam.lstrsum.dto.answer.AnswerAllFieldsDto;
import com.epam.lstrsum.email.EmailCollection;
import com.epam.lstrsum.model.Question;
import com.epam.lstrsum.model.Subscription;
import com.epam.lstrsum.model.User;
import com.epam.lstrsum.persistence.SubscriptionRepository;
import com.epam.lstrsum.service.QuestionService;
import com.epam.lstrsum.service.SubscriptionService;
import com.epam.lstrsum.service.UserService;
import com.mongodb.DBRef;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionServiceImpl implements SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionAggregator subscriptionAggregator;
    private final QuestionService questionService;
    private final UserService userService;
    private final MongoTemplate mongoTemplate;

    @Value("${spring.mail.username}")
    private String fromAddress;

    private static Address[] getAddressesFromEmails(Collection<String> emails) {
        return emails.stream()
                .map((s) -> {
                    try {
                        return new InternetAddress(s);
                    } catch (Exception e) {
                        log.warn("Could not parse email address: " + s, e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toArray(Address[]::new);
    }

    @Override
    public List<Subscription> findAll() {
        return subscriptionRepository.findAll();
    }

    @Override
    public List<Subscription> findAllSubscriptionsEntitiesToQuestionWithThisId(String questionId) {
        return subscriptionRepository.findAllByQuestionIdsContains(questionId);
    }

    @Override
    public Set<String> getEmailsToNotificateAboutNewQuestion(Question question) {
        return Stream.concat(question.getAllowedSubs().stream(), userService.findAllActive().stream())
                .map(User::getEmail)
                .filter(e -> !e.equalsIgnoreCase(fromAddress))
                .collect(Collectors.toSet());
    }

    @Override
    public Set<String> getEmailsToNotificateAboutNewAnswer(String questionId) {
        Set<String> emailsWithNoDuplicates = new HashSet<>();
        emailsWithNoDuplicates.addAll(getEmailsOfAuthorAndAllowedSubsOfQuestion(questionId));
        emailsWithNoDuplicates.addAll(getEmailsOfSubscribersOfQuestion(questionId));

        return emailsWithNoDuplicates;
    }

    @Override
    public List<String> getEmailsOfSubscribersOfQuestion(String questionId) {
        return findAllSubscriptionsEntitiesToQuestionWithThisId(questionId).stream()
                .map(s -> s.getUserId().getEmail())
                .filter(e -> !e.equalsIgnoreCase(fromAddress))
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getEmailsOfAuthorAndAllowedSubsOfQuestion(String questionId) {
        Question question = questionService.getQuestionById(questionId);

        List<String> emails =
                Stream.concat(question.getAllowedSubs().stream(), userService.findAllActive().stream())
                        .map(User::getEmail)
                        .filter(e -> !e.equalsIgnoreCase(fromAddress))
                        .collect(Collectors.toList());
        emails.add(question.getAuthorId().getEmail());

        return emails;
    }

    @Override
    public void addOrUpdate(String userId, List<String> questionIds) {
        // TODO: 16.08.17 it will add user even if user is not exists
        mongoTemplate.upsert(
                new Query(Criteria.where("userId").is(new DBRef(User.USER_COLLECTION_NAME, userId))),
                new Update().addToSet("questionIds").each(
                        questionIds.stream()
                                .map(question -> new DBRef(Question.QUESTION_COLLECTION_NAME, question))
                                .toArray()),
                Subscription.class
        );
    }

    @Component
    @RequiredArgsConstructor
    public static class QuestionEmailCollectionAdapter implements EmailCollection<Question> {
        private final SubscriptionService subscriptionService;

        @Override
        public Address[] getEmailAddresses(Question question) {
            return getAddressesFromEmails(
                    new HashSet<>(subscriptionService.getEmailsToNotificateAboutNewQuestion(question)));
        }
    }

    @Component
    @RequiredArgsConstructor
    public static class AnswerEmailCollectionAdapter implements EmailCollection<AnswerAllFieldsDto> {
        private final SubscriptionService subscriptionService;

        @Override
        public Address[] getEmailAddresses(AnswerAllFieldsDto answer) {
            return getAddressesFromEmails(
                    new HashSet<>(subscriptionService.getEmailsToNotificateAboutNewAnswer(
                            answer.getQuestion().getQuestionId()
                    )));
        }
    }
}
