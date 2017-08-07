package com.epam.lstrsum.service;

import com.epam.lstrsum.dto.answer.AnswerAllFieldsDto;
import com.epam.lstrsum.dto.question.QuestionAllFieldsDto;
import com.epam.lstrsum.email.EmailCollection;
import com.epam.lstrsum.model.Question;
import com.epam.lstrsum.model.Subscription;
import com.epam.lstrsum.model.User;
import com.epam.lstrsum.persistence.SubscriptionRepository;
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
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final QuestionService questionService;

    public List<Subscription> findAll() {
        return subscriptionRepository.findAll();
    }

    public List<Subscription> findAllSubscriptionsEntitiesToQuestionWithThisId(String questionId) {
        return subscriptionRepository.findAllByQuestionIdsContains(questionId);
    }

    public Set<String> getEmailsToNotificateAboutNewQuestion(String questionId) {
        Question question = questionService.getQuestionById(questionId);

        return question.getAllowedSubs().stream().map(User::getEmail).collect(Collectors.toSet());
    }

    public Set<String> getEmailsToNotificateAboutNewAnswer(String questionId) {
        Set<String> emailsWithNoDuplicates = new HashSet<>();
        emailsWithNoDuplicates.addAll(getEmailsOfAuthorAndAllowedSubsOfQuestion(questionId));
        emailsWithNoDuplicates.addAll(getEmailsOfSubscribersOfQuestion(questionId));

        return emailsWithNoDuplicates;
    }

    List<String> getEmailsOfSubscribersOfQuestion(String questionId) {
        return findAllSubscriptionsEntitiesToQuestionWithThisId(questionId).stream()
                .map(s -> s.getUserId().getEmail()).collect(Collectors.toList());
    }

    List<String> getEmailsOfAuthorAndAllowedSubsOfQuestion(String questionId) {
        Question question = questionService.getQuestionById(questionId);

        List<String> emails = question.getAllowedSubs().stream().map(User::getEmail).collect(Collectors.toList());
        emails.add(question.getAuthorId().getEmail());

        return emails;
    }

    @Component
    public class QuestionEmailCollectionAdapter implements EmailCollection<QuestionAllFieldsDto> {
        @Override
        public Address[] getEmailAddresses(QuestionAllFieldsDto question) {
            return getAddressesFromEmails(
                    new HashSet<>(getEmailsToNotificateAboutNewQuestion(question.getQuestionId())));
        }
    }

    @Component
    public class AnswerEmailCollectionAdapter implements EmailCollection<AnswerAllFieldsDto> {
        @Override
        public Address[] getEmailAddresses(AnswerAllFieldsDto answer) {
            return getAddressesFromEmails(
                    new HashSet<>(getEmailsToNotificateAboutNewAnswer(answer.getQuestionId().getQuestionId())));
        }
    }

    private Address[] getAddressesFromEmails(Collection<String> emails) {
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
