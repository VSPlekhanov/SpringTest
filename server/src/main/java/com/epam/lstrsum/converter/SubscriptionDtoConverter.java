package com.epam.lstrsum.converter;

import com.epam.lstrsum.dto.subscription.SubscriptionAllFieldsDto;
import com.epam.lstrsum.model.Subscription;
import com.epam.lstrsum.service.QuestionService;
import com.epam.lstrsum.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SubscriptionDtoConverter implements AllFieldModelDtoConverter<Subscription, SubscriptionAllFieldsDto> {

    private final UserService userService;
    private final QuestionService questionService;

    @Override
    public SubscriptionAllFieldsDto modelToAllFieldsDto(Subscription subscription) {
        return new SubscriptionAllFieldsDto(subscription.getSubscriptionId(),
                userService.modelToBaseDto(subscription.getUserId()),
                questionService.subscriptionsToListOfQuestionBaseDto(subscription.getQuestionIds()));
    }
}
