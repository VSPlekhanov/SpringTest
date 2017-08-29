package com.epam.lstrsum.aggregators;

import com.epam.lstrsum.converter.UserDtoMapper;
import com.epam.lstrsum.exception.NoSuchUserException;
import com.epam.lstrsum.persistence.UserRepository;
import com.epam.lstrsum.testutils.InstantiateUtil;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Collections;
import java.util.Optional;

import static com.epam.lstrsum.testutils.InstantiateUtil.initList;
import static com.epam.lstrsum.testutils.InstantiateUtil.someString;
import static com.epam.lstrsum.testutils.InstantiateUtil.someTelescopeDataDto;
import static com.epam.lstrsum.testutils.InstantiateUtil.someUser;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

public class UserAggregatorTest {
    @Mock
    private UserDtoMapper userMapper;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserAggregator aggregator;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
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

        verify(userMapper, times(1)).usersToListOfUserBaseDtos(any());
    }

    @Test
    public void userTelescopeInfoDtoToUser() throws Exception {
        aggregator.userTelescopeInfoDtoToUser(someTelescopeDataDto(), someString(), Collections.emptyList());

        verify(userMapper, times(1)).userTelescopeInfoDtoToUser(any(), anyString(), any());
    }

    @Test
    public void findByEmail() {
        doReturn(Optional.of(someUser())).when(userRepository).findByEmailIgnoreCase(anyString());

        aggregator.findByEmail(someString());

        verify(userRepository).findByEmailIgnoreCase(anyString());
    }

    @Test(expected = NoSuchUserException.class)
    public void findByEmailWithEmpyOptional() {
        doReturn(Optional.empty()).when(userRepository).findByEmailIgnoreCase(anyString());

        aggregator.findByEmail(someString());
    }
}
