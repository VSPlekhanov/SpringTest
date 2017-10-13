package com.epam.lstrsum.controller;

import com.epam.lstrsum.annotation.NotEmptyString;
import com.epam.lstrsum.service.SubscriptionService;
import com.epam.lstrsum.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/subscription")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final UserRuntimeRequestComponent userRuntimeRequestComponent;
    private final UserService userService;

    @PutMapping("/{question}")
    public ResponseEntity subscribe(@PathVariable @NotEmptyString String questionId) {
        subscriptionService.addOrUpdate(
                userService.findUserByEmail(userRuntimeRequestComponent.getEmail()).getUserId(),
                questionId
        );

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
