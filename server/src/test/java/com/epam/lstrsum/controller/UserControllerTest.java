package com.epam.lstrsum.controller;

import com.epam.lstrsum.SetUpDataBaseCollections;
import com.epam.lstrsum.model.User;
import com.epam.lstrsum.service.TelescopeService;
import com.epam.lstrsum.service.UserService;
import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static com.epam.lstrsum.testutils.InstantiateUtil.someString;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;

public class UserControllerTest extends SetUpDataBaseCollections {

    @Autowired
    private TestRestTemplate restTemplate;

    @Mock
    private UserService userService;

    @Mock
    private TelescopeService telescopeService;

    @InjectMocks
    private UserController controller;

    @Test
    public void getListOfUsers() throws Exception {
        ResponseEntity<List<User>> responseEntity = restTemplate.exchange("/api/user",
                HttpMethod.GET, null, new ParameterizedTypeReference<List<User>>() {
                });
        List<User> actualList = responseEntity.getBody();
        //validate
        assertThat(actualList.size(), is(7));
        List<String> actualIds = actualList.stream().map(User::getUserId).collect(collectingAndThen(toList(), ImmutableList::copyOf));
        assertThat(actualIds, containsInAnyOrder("1u", "2u", "3u", "4u", "5u", "6u", "7u"));
    }

    @Test
    public void getUserPhotoByUri() {
        controller.getUserPhotoByUri(someString());

        verify(telescopeService).getUserPhotoByUri(anyString());
    }
}
