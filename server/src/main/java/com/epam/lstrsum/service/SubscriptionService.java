package com.epam.lstrsum.service;

import com.epam.lstrsum.model.Subscription;
import com.epam.lstrsum.persistence.SubscriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;

    @Autowired
    public SubscriptionService(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    public List<Subscription> findAll() {
        return subscriptionRepository.findAll();
    }
}
