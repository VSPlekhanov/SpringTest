package com.epam.lstrsum.aggregators;

import com.epam.lstrsum.converter.contract.AllFieldModelDtoConverter;
import com.epam.lstrsum.converter.SubscriptionDtoMapper;
import com.epam.lstrsum.converter.UserDtoMapper;
import com.epam.lstrsum.dto.subscription.SubscriptionAllFieldsDto;
import com.epam.lstrsum.model.Subscription;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SubscriptionAggregator implements
        AllFieldModelDtoConverter<Subscription, SubscriptionAllFieldsDto> {
    private final SubscriptionDtoMapper subscriptionMapper;
    private final UserDtoMapper userMapper;

    private final QuestionAggregator questionAggregator;

    @Override
    public SubscriptionAllFieldsDto modelToAllFieldsDto(Subscription subscription) {
        return subscriptionMapper.modelToAllFieldsDto(
                subscription,
                userMapper.modelToBaseDto(subscription.getUserId()),
                questionAggregator.subscriptionsToListOfQuestionBaseDto(subscription.getQuestionIds())
        );
    }
}
