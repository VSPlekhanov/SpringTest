package com.epam.lstrsum.service;

import com.epam.lstrsum.SetUpDataBaseCollections;
import com.epam.lstrsum.exception.NoSuchUserException;
import com.epam.lstrsum.model.User;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class UserServiceTest extends SetUpDataBaseCollections {

    static final String NON_EXISTING_EMAIL = "email@test.com";
    static final String EXISTING_EMAIL = "John_Doe@epam.com";
    static final String NON_EXISTING_USER_ID = "1123";
    static final String EXISTING_USER_ID = "1u";

    @Autowired
    private UserService userService;

    @Test
    public void getUserByEmail() {
        User user = userService.getUserByEmail(EXISTING_EMAIL);

        assertNotNull(user);
        assertEquals(user.getEmail(), EXISTING_EMAIL);
    }

    @Test
    public void findAll() {
        List<User> allUsers = userService.findAll();

        assertNotNull(allUsers);
        assertThat(allUsers.size(), is(6));
        assertThat(allUsers, hasItem(Matchers.<User>hasProperty("email", equalTo(EXISTING_EMAIL))));
    }

    @Test
    public void getUserById() {
        User user = userService.getUserById(EXISTING_USER_ID);

        assertNotNull(user);
        assertEquals(user.getUserId(), EXISTING_USER_ID);
    }

    @Test(expected = NoSuchUserException.class)
    public void getUserByEmailWithNonExistingEmail() {
        userService.getUserByEmail(NON_EXISTING_EMAIL);
    }

    @Test(expected = NoSuchUserException.class)
    public void getUserByIdWithNonExistingId() {
        userService.getUserById(NON_EXISTING_USER_ID);
    }
}
