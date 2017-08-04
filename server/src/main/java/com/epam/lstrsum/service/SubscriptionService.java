package com.epam.lstrsum.service;

import com.epam.lstrsum.dto.answer.AnswerAllFieldsDto;
import com.epam.lstrsum.dto.question.QuestionAllFieldsDto;
import com.epam.lstrsum.email.EmailCollection;
import com.epam.lstrsum.model.Question;
import com.epam.lstrsum.model.Subscription;
import com.epam.lstrsum.model.User;
import com.epam.lstrsum.persistence.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final QuestionService questionService;

    public List<Subscription> findAll() {
        return subscriptionRepository.findAll();
    }

    public List<Subscription> findAllSubscriptionsEntitiesToQuestionWithThisId(String questionId) {
        return subscriptionRepository.findAllByQuestionIdsContains(questionId);
    }

    public List<String> getEmailsToNotificateAboutNewQuestion(String questionId) {
        Question question = questionService.getQuestionById(questionId);

        return question.getAllowedSubs().stream().map(User::getEmail).collect(Collectors.toList());
    }

    public Set<String> getEmailsToNotificateAboutNewAnswer(String questionId) {
        Set<String> emailsWithNoDups = new HashSet<>();
        emailsWithNoDups.addAll(getEmailsOfAuthorAndAllowedSubsOfQuestion(questionId));
        emailsWithNoDups.addAll(getEmailsOfSubscribersOfQuestion(questionId));

        return emailsWithNoDups;
    }

    List<String> getEmailsOfSubscribersOfQuestion(String questionId) {
        return subscriptionRepository.findAllByQuestionIdsContains(questionId).stream()
                .map(s -> s.getUserId().getEmail()).collect(Collectors.toList());
    }

    List<String> getEmailsOfAuthorAndAllowedSubsOfQuestion(String questionId) {
        Question question = questionService.getQuestionById(questionId);

        List<String> emails = question.getAllowedSubs().stream().map(User::getEmail).collect(Collectors.toList());
        emails.add(question.getAuthorId().getEmail());

        return emails;
    }

    @Component
    public class RequestEmailCollectionAdapter implements EmailCollection<QuestionAllFieldsDto> {
        @Override
        public Set<String> getEmails(QuestionAllFieldsDto request) {
            return new HashSet<>(getEmailsToNotificateAboutNewQuestion(request.getQuestionId()));
        }
    }

    @Component
    public class AnswerEmailCollectionAdapter implements EmailCollection<AnswerAllFieldsDto> {
        @Override
        public Set<String> getEmails(AnswerAllFieldsDto answer) {
            return new HashSet<>(getEmailsToNotificateAboutNewAnswer(answer.getParentId().getQuestionId()));
        }
    }
}
