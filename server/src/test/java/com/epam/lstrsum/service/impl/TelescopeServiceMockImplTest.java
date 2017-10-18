package com.epam.lstrsum.service.impl;

import com.epam.lstrsum.service.impl.mock.TelescopeServiceMockImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.actuate.metrics.CounterService;

import java.io.IOException;
import java.util.HashSet;

import static com.epam.lstrsum.testutils.InstantiateUtil.someString;
import static java.util.Collections.singleton;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

public class TelescopeServiceMockImplTest {

    @Mock
    private CounterService counterService;

    @InjectMocks
    private TelescopeServiceMockImpl telescopeServiceMock;

    @Before
    public void setUp() throws IOException {
        initMocks(this);

        telescopeServiceMock.init();
    }

    @Test
    public void getUsersInfoByFullName() throws Exception {
        assertThat(telescopeServiceMock.getUsersInfoByFullName(someString(), 5))
                .isNotNull();

        verify(counterService, times(1)).increment(eq("telescope.get.users.info.by.full.name"));

    }

    @Test
    public void getUserPhotoByUri() throws Exception {
        assertThat(telescopeServiceMock.getUserPhotoByUri(someString()))
                .isNotEmpty();

        verify(counterService, times(1)).increment(eq("telescope.get.photo"));
    }

    @Test
    public void getUsersInfoByEmails() throws Exception {
        assertThat(telescopeServiceMock.getUsersInfoByEmails(new HashSet<>(singleton(someString()))))
                .isNotNull();

        verify(counterService, times(1)).increment(eq("telescope.get.users.info.by.emails"));
    }

}