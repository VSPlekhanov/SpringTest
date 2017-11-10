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

    @PutMapping("/subscribe/{questionId}")
    public ResponseEntity subscribe(@PathVariable @NotEmptyString String questionId) {
        String email = userRuntimeRequestComponent.getEmail();
        boolean successSubscribe = currentUserInDistributionList() ?
                subscriptionService.subscribeForQuestionByUser(questionId, email) :
                subscriptionService.subscribeForQuestionByAllowedSub(questionId, email);

        return successSubscribe ? ResponseEntity.status(HttpStatus.NO_CONTENT).build()
                : ResponseEntity.status(HttpStatus.NOT_FOUND).build();

    }

    @PutMapping("/unsubscribe/{questionId}")
    public ResponseEntity unSubscribe(@PathVariable @NotEmptyString String questionId) {
        String email = userRuntimeRequestComponent.getEmail();
        boolean successSubscribe = currentUserInDistributionList() ?
                subscriptionService.unsubscribeForQuestionByUser(questionId, email) :
                subscriptionService.unsubscribeForQuestionByAllowedSub(questionId, email);

        return successSubscribe ? ResponseEntity.status(HttpStatus.NO_CONTENT).build()
                : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    private boolean currentUserInDistributionList() {
        return userRuntimeRequestComponent.isInDistributionList();
    }
}
