package com.epam.lstrsum.service.impl;

import com.epam.lstrsum.service.ElasticSearchServiceMockImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.actuate.metrics.CounterService;

import static com.epam.lstrsum.testutils.InstantiateUtil.someInt;
import static com.epam.lstrsum.testutils.InstantiateUtil.someString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

public class ElasticSearchServiceMockImplTest {

    @Mock
    private CounterService counterService;

    @InjectMocks
    private ElasticSearchServiceMockImpl elasticSearchServiceMock;

    @Before
    public void setUp() {
        initMocks(this);
    }

    @Test
    public void smartSearch() {
        assertThat(elasticSearchServiceMock.smartSearch(someString(), someInt(), someInt())).isNotNull();

        verify(counterService, times(1)).increment(eq("elastic.smart.search"));
    }
}