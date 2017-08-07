package com.epam.lstrsum.aggregators;

import com.epam.lstrsum.InstantiateUtil;
import com.epam.lstrsum.converter.UserDtoMapper;
import com.epam.lstrsum.persistence.UserRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static com.epam.lstrsum.InstantiateUtil.initList;
import static com.epam.lstrsum.InstantiateUtil.someUser;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

public class UserAggregatorTest {
    private UserAggregator aggregator;

    @Mock
    private UserDtoMapper userMapper;

    @Mock
    private UserRepository userRepository;

    @Before
    public void setUp() {
        initMocks(this);
        aggregator = new UserAggregator(userMapper, userRepository);
    }

    @Test
    public void modelToAllFieldsDto() throws Exception {
        aggregator.modelToAllFieldsDto(someUser());

        verify(userMapper, times(1)).modelToAllFieldsDto(any());
    }

    @Test
    public void modelToBaseDto() throws Exception {
        aggregator.modelToBaseDto(someUser());

        verify(userMapper, times(1)).modelToBaseDto(any());
    }

    @Test
    public void allowedSubsToListOfUserBaseDtos() throws Exception {
        final int size = 2;
        aggregator.allowedSubsToListOfUserBaseDtos(initList(InstantiateUtil::someUser, size));

        verify(userMapper, times(1)).allowedSubsToListOfUserBaseDtos(any());
    }
}
