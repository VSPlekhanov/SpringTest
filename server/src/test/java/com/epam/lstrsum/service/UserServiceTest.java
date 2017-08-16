package com.epam.lstrsum.service;

import com.epam.lstrsum.SetUpDataBaseCollections;
import com.epam.lstrsum.aggregators.UserAggregator;
import com.epam.lstrsum.enums.UserRoleType;
import com.epam.lstrsum.exception.NoSuchUserException;
import com.epam.lstrsum.model.User;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.epam.lstrsum.testutils.InstantiateUtil.*;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;


public class UserServiceTest extends SetUpDataBaseCollections {
    @Autowired
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
        assertEquals(userService.findAllWithRole(UserRoleType.EXTENDED_USER).size(), 5);
    }

    @Test
    public void findAll() {
        final List<User> allUsers = userService.findAll();

        assertNotNull(allUsers);
        assertThat(allUsers.size(), is(7));
        assertThat(allUsers, hasItem(Matchers.<User>hasProperty("email", equalTo(SOME_USER_EMAIL))));
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
        final List<User> alreadyInBase = userService.findAll();

        final User[] notInBaseUsers = {someUser(), someUser(), someUser()};
        final List<User> concat = Stream.concat(Arrays.stream(notInBaseUsers), alreadyInBase.stream())
                .collect(Collectors.toList());

        final long actual = userService.addIfNotExistAllWithRole(
                concat.stream().map(User::getEmail).collect(Collectors.toList()), singletonList(UserRoleType.SIMPLE_USER)
        );

        assertEquals(actual, notInBaseUsers.length);
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
    public void getUserInfoByFullName() {
        userService.getUserInfoByFullName(someString(), someInt());

        verify(telescopeService, times(1)).getUsersInfoByFullName(anyString(), anyInt());
    }

    @Test
    public void getUserPhotoByUri() {
        userService.getUserPhotoByUri(someString());

        verify(telescopeService, times(1)).getUserPhotoByUri(anyString());
    }

    @Test
    public void addNewUserByEmail() {
        doReturn(someUser()).when(userAggregator).userTelescopeInfoDtoToUser(any(), anyString(), any());
        doReturn(someTelescopeEmployeeEntityDto()).when(telescopeService).getUserInfoByEmail(anyString());

        assertThat(userService.addNewUserByEmail(SOME_USER_EMAIL, someRoles())).isNotNull();
    }
}
