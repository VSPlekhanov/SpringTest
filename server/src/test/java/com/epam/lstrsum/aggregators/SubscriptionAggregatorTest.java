package com.epam.lstrsum.aggregators;

import com.epam.lstrsum.converter.SubscriptionDtoMapper;
import com.epam.lstrsum.converter.UserDtoMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static com.epam.lstrsum.InstantiateUtil.someSubscription;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

public class SubscriptionAggregatorTest {
    private SubscriptionAggregator aggregator;

    @Mock
    private SubscriptionDtoMapper subscriptionMapper;

    @Mock
    private UserDtoMapper userMapper;

    @Mock
    private QuestionAggregator questionAggregator;

    @Before
    public void setUp() {
        initMocks(this);
        aggregator = new SubscriptionAggregator(subscriptionMapper, userMapper, questionAggregator);
    }

    @Test
    public void modelToAllFieldsDto() throws Exception {
        aggregator.modelToAllFieldsDto(someSubscription());

        verify(subscriptionMapper, times(1)).modelToAllFieldsDto(any(), any(), any());
    }
}
