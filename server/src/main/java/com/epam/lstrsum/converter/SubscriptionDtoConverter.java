package com.epam.lstrsum.converter;

import com.epam.lstrsum.dto.subscription.SubscriptionAllFieldsDto;
import com.epam.lstrsum.model.Subscription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SubscriptionDtoConverter implements AllFieldModelDtoConverter<Subscription, SubscriptionAllFieldsDto> {

    @Autowired
    private UserDtoConverter userConverter;
    @Autowired
    private QuestionDtoConverter questionDtoConverter;

    @Override
    public SubscriptionAllFieldsDto modelToAllFieldsDto(Subscription subscription) {
        return new SubscriptionAllFieldsDto(subscription.getSubscriptionId(),
                userConverter.modelToBaseDto(subscription.getUserId()),
                questionDtoConverter.subscriptionsToListOfQuestionBaseDto(subscription.getQuestionIds()));
    }
}
