package com.epam.lstrsum.email.service.impl;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.actuate.metrics.CounterService;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

public class ExchangeServiceHelperMockImplTest {

    @Mock
    private CounterService counterService;

    @InjectMocks
    private ExchangeServiceHelperMockImpl exchangeServiceHelperMock;

    private String distributionList = "distributionList";

    @Before
    public void setUp() {
        initMocks(this);
        exchangeServiceHelperMock.setDistributionList(distributionList);

    }

    @Test
    public void resolveGroupWithDistributionList() throws Exception {
        assertThat(exchangeServiceHelperMock.resolveGroup(distributionList))
                .hasSize(16);

        verify(counterService, times(1)).increment(eq("exchange.service.expand.group"));
    }

    @Test
    public void resolveGroupWithoutDistributionList() {
        assertThat(exchangeServiceHelperMock.resolveGroup("someGroupName"))
                .containsOnly("someGroupName");

        verify(counterService, times(1)).increment(eq("exchange.service.expand.group"));
    }

}