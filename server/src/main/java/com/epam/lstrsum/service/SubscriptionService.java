package com.epam.lstrsum.service;

import com.epam.lstrsum.dto.subscription.SubscriptionAllFieldsDto;
import com.epam.lstrsum.model.Question;
import com.epam.lstrsum.model.Subscription;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public interface SubscriptionService {
    List<Subscription> findAll();

    List<Subscription> findAllSubscriptionsEntitiesToQuestionWithThisId(String questionId);

    Set<String> getEmailsToNotificateAboutNewQuestion(Question question);

    Set<String> getEmailsToNotificateAboutNewAnswer(String questionId);

    List<String> getEmailsOfSubscribersOfQuestion(String questionId);

    List<String> getEmailsOfAuthorAndAllowedSubsOfQuestion(String questionId);

    void addOrUpdate(String userId, List<String> questionIds);

    default void addOrUpdate(String userId, String questionId) {
        addOrUpdate(userId, Collections.singletonList(questionId));
    }

    List<SubscriptionAllFieldsDto> findAllSubscriptionsAllFieldsDto();
}
