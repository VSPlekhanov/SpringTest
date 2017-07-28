package com.epam.lstrsum.service;

import com.epam.lstrsum.model.Request;
import com.epam.lstrsum.model.Subscription;
import com.epam.lstrsum.model.User;
import com.epam.lstrsum.persistence.SubscriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final RequestService requestService;

    @Autowired
    public SubscriptionService(SubscriptionRepository subscriptionRepository, RequestService requestService) {
        this.subscriptionRepository = subscriptionRepository;
        this.requestService = requestService;
    }

    public List<Subscription> findAll() {
        return subscriptionRepository.findAll();
    }

    public List<Subscription> findAllSubscriptionsEntitiesToRequestWithThisId(String requestId) {
        return subscriptionRepository.findAllByRequestIdsContains(requestId);
    }

    public List<String> getEmailsToNotificateAboutNewRequest(String requestId) {
        Request request = requestService.getRequestById(requestId);

        return request.getAllowedSubs().stream().map(User::getEmail).collect(Collectors.toList());
    }

    public Set<String> getEmailsToNotificateAboutNewAnswer(String requestId) {
        Set<String> emailsWithNoDups = new HashSet<>();
        emailsWithNoDups.addAll(getEmailsOfAuthorAndAllowedSubsOfRequest(requestId));
        emailsWithNoDups.addAll(getEmailsOfSubscribersOfRequest(requestId));

        return emailsWithNoDups;
    }

    List<String> getEmailsOfSubscribersOfRequest(String requestId) {
        return subscriptionRepository.findAllByRequestIdsContains(requestId).stream()
                .map(s -> s.getUserId().getEmail()).collect(Collectors.toList());
    }

    List<String> getEmailsOfAuthorAndAllowedSubsOfRequest(String requestId) {
        Request request = requestService.getRequestById(requestId);

        List<String> emails = request.getAllowedSubs().stream().map(User::getEmail).collect(Collectors.toList());
        emails.add(request.getAuthorId().getEmail());

        return emails;
    }
}
