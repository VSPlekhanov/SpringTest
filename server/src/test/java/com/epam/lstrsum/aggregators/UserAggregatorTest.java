package com.epam.lstrsum.aggregators;

import com.epam.lstrsum.converter.UserDtoMapper;
import com.epam.lstrsum.enums.UserRoleType;
import com.epam.lstrsum.exception.NoSuchUserException;
import com.epam.lstrsum.persistence.UserRepository;
import com.epam.lstrsum.testutils.InstantiateUtil;
import com.epam.lstrsum.utils.MessagesHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.EnumSet;
import java.util.Optional;

import static com.epam.lstrsum.testutils.InstantiateUtil.someString;
import static com.epam.lstrsum.testutils.InstantiateUtil.someTelescopeDataDto;
import static com.epam.lstrsum.testutils.InstantiateUtil.someUser;
import static com.epam.lstrsum.utils.FunctionalUtil.getListWithSize;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;


public class UserAggregatorTest {
    @Mock
    private UserDtoMapper userMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MessagesHelper messagesHelper;

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
    public void usersToListOfBaseDtos() throws Exception {
        final int size = 2;
        aggregator.usersToListOfBaseDtos(getListWithSize(InstantiateUtil::someUser, size));

        verify(userMapper, times(1)).usersToListOfBaseDtos(any());
    }

    @Test
    public void userTelescopeInfoDtoToUser() throws Exception {
        aggregator.userTelescopeInfoDtoToUser(someTelescopeDataDto(), someString(), EnumSet.noneOf(UserRoleType.class));

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
        when(messagesHelper.get(anyString())).thenReturn("");
        aggregator.findByEmail(someString());
    }
}
