package com.epam.lstrsum.converter;

import com.epam.lstrsum.SetUpDataBaseCollections;
import com.epam.lstrsum.model.Subscription;
import com.epam.lstrsum.persistence.SubscriptionRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.stream.Collectors;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class SubscriptionDtoConverterTest extends SetUpDataBaseCollections {
    @Autowired
    private SubscriptionDtoConverter subscriptionConverter;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private UserDtoConverter userConverter;

    @Autowired
    private RequestDtoConverter requestConverter;

    @Test
    public void ConvertModelToAllFieldsDtoReturnsExcpectedValue() throws Exception {
        Subscription subscription = subscriptionRepository.findOne("1u_1s");


        assertThat(subscriptionConverter.modelToAllFieldsDto(subscription))
                .hasFieldOrPropertyWithValue("subscriptionId", subscription.getSubscriptionId())
                .hasFieldOrPropertyWithValue("userId", userConverter.modelToBaseDto(subscription.getUserId()))
                .hasFieldOrPropertyWithValue("requestIds", subscription.getRequestIds().stream()
                        .map(s -> requestConverter.modelToBaseDto(s))
                        .collect(Collectors.toList()));
    }
}
