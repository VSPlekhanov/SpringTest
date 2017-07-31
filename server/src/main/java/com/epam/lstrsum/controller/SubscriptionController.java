package com.epam.lstrsum.controller;

import com.epam.lstrsum.model.Subscription;
import com.epam.lstrsum.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/subscription")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @RequestMapping
    public List<Subscription> getListOfSubscriptions() {
        return subscriptionService.findAll();
    }
}
