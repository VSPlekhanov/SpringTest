package com.epam.lstrsum.service.impl;

import com.epam.lstrsum.dto.answer.AnswerAllFieldsDto;
import com.epam.lstrsum.email.EmailCollection;
import com.epam.lstrsum.model.Question;
import com.epam.lstrsum.model.Subscription;
import com.epam.lstrsum.model.User;
import com.epam.lstrsum.persistence.SubscriptionRepository;
import com.epam.lstrsum.service.QuestionService;
import com.epam.lstrsum.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.mail.Address;
import javax.mail.internet.InternetAddress;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionServiceImpl implements SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;
    private final QuestionService questionService;

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
        return question.getAllowedSubs().stream().map(User::getEmail).collect(Collectors.toSet());
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
                .map(s -> s.getUserId().getEmail()).collect(Collectors.toList());
    }

    @Override
    public List<String> getEmailsOfAuthorAndAllowedSubsOfQuestion(String questionId) {
        Question question = questionService.getQuestionById(questionId);

        List<String> emails = question.getAllowedSubs().stream().map(User::getEmail).collect(Collectors.toList());
        emails.add(question.getAuthorId().getEmail());

        return emails;
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
                            answer.getQuestionId().getQuestionId()
                    )));
        }
    }

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
}
