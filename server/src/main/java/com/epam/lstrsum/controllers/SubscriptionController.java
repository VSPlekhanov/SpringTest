package com.epam.lstrsum.controllers;

import com.epam.lstrsum.model.Subscription;
import com.epam.lstrsum.service.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/subscription")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @Autowired
    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @RequestMapping
    public List<Subscription> getListOfSubscriptions() {
        return subscriptionService.findAll();
    }
}
