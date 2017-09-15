package com.epam.lstrsum.service;

import com.epam.lstrsum.SetUpDataBaseCollections;
import com.epam.lstrsum.aggregators.UserAggregator;
import com.epam.lstrsum.dto.user.telescope.TelescopeDataDto;
import com.epam.lstrsum.dto.user.telescope.TelescopeEmployeeEntityDto;
import com.epam.lstrsum.enums.UserRoleType;
import com.epam.lstrsum.exception.NoSuchUserException;
import com.epam.lstrsum.model.User;
import lombok.val;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.epam.lstrsum.testutils.InstantiateUtil.EXISTING_USER_ID;
import static com.epam.lstrsum.testutils.InstantiateUtil.NON_EXISTING_USER_ID;
import static com.epam.lstrsum.testutils.InstantiateUtil.SOME_NOT_USER_EMAIL;
import static com.epam.lstrsum.testutils.InstantiateUtil.SOME_USER_EMAIL;
import static com.epam.lstrsum.testutils.InstantiateUtil.someString;
import static com.epam.lstrsum.testutils.InstantiateUtil.someTelescopeEmployeeEntityDto;
import static com.epam.lstrsum.testutils.InstantiateUtil.someUser;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anySetOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class UserServiceTest extends SetUpDataBaseCollections {

    @MockBean
    private TelescopeService telescopeService;

    @Autowired
    private UserService userService;

    @MockBean
    private UserAggregator userAggregator;

    @Test
    public void getUserByEmail() {
        User user = userService.findUserByEmail(SOME_USER_EMAIL);

        assertNotNull(user);
        assertEquals(user.getEmail(), SOME_USER_EMAIL);
    }

    public void findAllWithRole() {
        assertEquals(userService.findAllWithRole(UserRoleType.ROLE_EXTENDED_USER).size(), 5);
    }

    @Test
    public void findAll() {
        final List<User> allUsers = userService.findAll();

        assertThat(allUsers)
                .hasSize(7)
                .anySatisfy(user -> assertThat(user.getEmail()).isEqualTo(SOME_USER_EMAIL));
    }

    @Test
    public void updateActiveMoreOne() {
        final String johnDoe = "John_Doe@epam.com";
        final String bobHoplins = "Bob_Hoplins@epam.com";
        final long actual = userService.setActiveForAllAs(Arrays.asList(johnDoe, bobHoplins), false);

        assertEquals(actual, 2);
        assertEquals(userService.findUserByEmail(johnDoe).getIsActive(), false);
        assertEquals(userService.findUserByEmail(bobHoplins).getIsActive(), false);
    }

    @Test
    public void updateActiveNoOne() {
        final long actual = userService.setActiveForAllAs(Arrays.asList("first", "second", "third"), false);

        assertEquals(actual, 0);
    }

    @Test
    public void getUserById() {
        final User user = userService.findUserById(EXISTING_USER_ID);

        assertNotNull(user);
        assertEquals(user.getUserId(), EXISTING_USER_ID);
    }

    @Test
    public void addIfNotExistAllWithRole() {
        final List<String> alreadyInBaseEmails = userService.findAll().stream().map(User::getEmail).collect(Collectors.toList());
        List<TelescopeEmployeeEntityDto> dtos = Collections.singletonList(someTelescopeEmployeeEntityDto());
        final List<String> notInBaseEmails = dtos.stream()
                .map(TelescopeEmployeeEntityDto::getData)
                .map(data -> data.getEmail().get(0))
                .collect(Collectors.toList());

        final List<String> concat = Stream.concat(alreadyInBaseEmails.stream(), notInBaseEmails.stream()).collect(Collectors.toList());

        doReturn(dtos).when(telescopeService).getUsersInfoByEmails(anySetOf(String.class));
        when(userAggregator.userTelescopeInfoDtoToUser(any(), anyString(), any())).thenReturn(someUser());

        final long actual = userService.addIfNotExistAllWithRole(concat, singletonList(UserRoleType.ROLE_SIMPLE_USER));

        assertEquals(actual, notInBaseEmails.size());

        verify(telescopeService, times(1)).getUsersInfoByEmails(anySetOf(String.class));
        verify(userAggregator, times(1)).userTelescopeInfoDtoToUser(any(), anyString(), any());
    }

    @Test
    public void addIfNotExistAllWithRoleNoDataFromTelescope() {
        String someEmail = someString();
        TelescopeDataDto dataDto = TelescopeDataDto.builder().email(singletonList(someEmail)).build();
        TelescopeEmployeeEntityDto employeeEntityDto = TelescopeEmployeeEntityDto.builder().data(dataDto).build();
        List<TelescopeEmployeeEntityDto> dtoList = Collections.singletonList(employeeEntityDto);

        doReturn(dtoList).when(telescopeService).getUsersInfoByEmails(anySetOf(String.class));

        userService.addIfNotExistAllWithRole(Collections.singletonList(someEmail), singletonList(UserRoleType.ROLE_SIMPLE_USER));

        verify(telescopeService, times(1)).getUsersInfoByEmails(anySetOf(String.class));
    }

    @Test
    public void addIfNotExistAllWithRoleAlreadyAddedUser() {
        String someEmail = SOME_USER_EMAIL.toLowerCase();
        TelescopeDataDto dataDto =
                TelescopeDataDto.builder().email(singletonList(someEmail)).lastName(someString()).firstName(someString()).build();
        TelescopeEmployeeEntityDto employeeEntityDto = TelescopeEmployeeEntityDto.builder().data(dataDto).build();
        List<TelescopeEmployeeEntityDto> dtoList = Collections.singletonList(employeeEntityDto);

        doReturn(dtoList).when(telescopeService).getUsersInfoByEmails(anySetOf(String.class));

        userService.addIfNotExistAllWithRole(Collections.singletonList(someEmail), singletonList(UserRoleType.ROLE_SIMPLE_USER));

        verify(telescopeService, times(1)).getUsersInfoByEmails(anySetOf(String.class));
    }

    @Test(expected = NoSuchUserException.class)
    public void getUserByEmailWithNonExistingEmail() {
        userService.findUserByEmail(SOME_NOT_USER_EMAIL);
    }

    @Test(expected = NoSuchUserException.class)
    public void getUserByIdWithNonExistingId() {
        userService.findUserById(NON_EXISTING_USER_ID);
    }

    @Test
    public void findAllActive() {
        val allowedEmails = Arrays.asList("Tyler_Derden@mylo.com", "Ernest_Hemingway@epam.com", "Donald_Gardner@epam.com",
                "Bob_Hoplins@epam.com", "John_Doe@epam.com");

        assertThat(userService.findAllActive())
                .hasSize(5)
                .allSatisfy(user -> assertThat(allowedEmails).contains(user.getEmail()));
    }
}
