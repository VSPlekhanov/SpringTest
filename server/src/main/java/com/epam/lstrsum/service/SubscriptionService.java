package com.epam.lstrsum.service;

import com.epam.lstrsum.model.Question;

import java.util.Set;

public interface SubscriptionService {

    Set<String> getEmailsToNotifyAboutNewQuestionFromPortal(Question question);

    Set<String> getEmailsToNotifyAboutNewAnswerFromPortal(String questionId);

    Set<String> getEmailsToNotifyAboutNewQuestionFromEmail(Question question);

    Set<String> getEmailsToNotifyAboutNewAnswerFromEmail(String questionId);

    boolean subscribeForQuestionByUser(String questionId, String email);

    boolean unsubscribeForQuestionByUser(String questionId, String email);

    boolean subscribeForQuestionByAllowedSub(String questionId, String email);

    boolean unsubscribeForQuestionByAllowedSub(String questionId, String email);
}
