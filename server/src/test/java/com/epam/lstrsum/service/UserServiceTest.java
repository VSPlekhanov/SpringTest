package com.epam.lstrsum.service;

import com.epam.lstrsum.SetUpDataBaseCollections;
import com.epam.lstrsum.enums.UserRoleType;
import com.epam.lstrsum.exception.NoSuchUserException;
import com.epam.lstrsum.model.User;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.epam.lstrsum.testutils.InstantiateUtil.someUser;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class UserServiceTest extends SetUpDataBaseCollections {

    static final String NON_EXISTING_EMAIL = "email@test.com";
    static final String EXISTING_EMAIL = "John_Doe@epam.com";
    static final String NON_EXISTING_USER_ID = "1123";
    static final String EXISTING_USER_ID = "1u";

    @Autowired
    private UserService userService;

    @Test
    public void getUserByEmail() {
        User user = userService.findUserByEmail(EXISTING_EMAIL);

        assertNotNull(user);
        assertEquals(user.getEmail(), EXISTING_EMAIL);
    }

    public void findAllWithRole() {
        assertEquals(userService.findAllWithRole(UserRoleType.ROLE_EXTENDED_USER).size(), 5);
    }

    @Test
    public void findAll() {
        final List<User> allUsers = userService.findAll();

        assertNotNull(allUsers);
        assertThat(allUsers.size(), is(7));
        assertThat(allUsers, hasItem(Matchers.<User>hasProperty("email", equalTo(EXISTING_EMAIL))));
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
                concat.stream().map(User::getEmail).collect(Collectors.toList()), Collections.singletonList(UserRoleType.ROLE_SIMPLE_USER)
        );

        assertEquals(actual, notInBaseUsers.length);
    }

    @Test(expected = NoSuchUserException.class)
    public void getUserByEmailWithNonExistingEmail() {
        userService.findUserByEmail(NON_EXISTING_EMAIL);
    }

    @Test(expected = NoSuchUserException.class)
    public void getUserByIdWithNonExistingId() {
        userService.findUserById(NON_EXISTING_USER_ID);
    }
}
