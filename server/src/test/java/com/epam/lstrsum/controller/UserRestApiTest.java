package com.epam.lstrsum.controller;

import com.epam.lstrsum.SetUpDataBaseCollections;
import com.epam.lstrsum.model.User;
import com.epam.lstrsum.service.TelescopeService;
import com.epam.lstrsum.testutils.AssertionUtils;
import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static com.epam.lstrsum.testutils.AssertionUtils.hasStatusBadRequest;
import static com.epam.lstrsum.testutils.AssertionUtils.hasStatusInternalServerError;
import static com.epam.lstrsum.testutils.AssertionUtils.hasStatusOk;
import static com.epam.lstrsum.testutils.InstantiateUtil.someString;
import static com.epam.lstrsum.testutils.InstantiateUtil.someTelescopeEmployeeEntityDtos;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class UserRestApiTest extends SetUpDataBaseCollections {
    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private TelescopeService telescopeService;

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
    public void getUserInfoByValidFullName() {
        int defaultMaxUsersAmountInResult = 5;
        String fullName = someString();

        when(telescopeService.getUsersInfoByFullName(fullName, defaultMaxUsersAmountInResult))
                .thenReturn(someTelescopeEmployeeEntityDtos());

        assertThat( restTemplate.exchange(
                "/api/user/telescope/info?fullName=" + fullName, HttpMethod.GET, null, Object.class
        )).satisfies(AssertionUtils::hasStatusOk);

        verify(telescopeService, times(1)).getUsersInfoByFullName(fullName, defaultMaxUsersAmountInResult);
    }

    @Test
    public void getUserInfoByValidFullNameAndUsersAmount() {
        int maxUsersAmountInResult = 77;
        String fullName = someString();

        when(telescopeService.getUsersInfoByFullName(fullName, maxUsersAmountInResult))
                .thenReturn(someTelescopeEmployeeEntityDtos());

        assertThat( restTemplate.exchange(
                "/api/user/telescope/info?fullName=" + fullName + "&maxUsersAmountInResult=" + maxUsersAmountInResult,
                HttpMethod.GET, null, Object.class
        )).satisfies(AssertionUtils::hasStatusOk);

        verify(telescopeService, times(1)).getUsersInfoByFullName(fullName, maxUsersAmountInResult);
    }

    @Test
    public void getUserInfoByMissingParameters() {
        assertThat( restTemplate.exchange(
                "/api/user/telescope/info", HttpMethod.GET, null, Object.class
        )).satisfies(AssertionUtils::hasStatusBadRequest);

        verifyZeroInteractions(telescopeService);
    }

    @Test
    public void getUserInfoByEmptyFullname() {
        assertThat( restTemplate.exchange(
                "/api/user/telescope/info?fullName=", HttpMethod.GET, null, Object.class
        )).satisfies(AssertionUtils::hasStatusInternalServerError);

        verifyZeroInteractions(telescopeService);
    }

    @Test
    public void getUserInfoByInvalidUsersAmount() {
        assertThat( restTemplate.exchange(
                "/api/user/telescope/info?fullName=" + someString() + "&maxUsersAmountInResult=" + 5001,
                HttpMethod.GET, null, Object.class
        )).satisfies(AssertionUtils::hasStatusInternalServerError);

        verifyZeroInteractions(telescopeService);
    }

    @Test
    public void getUserPhotoByUri() {
        String uri = someString();

        when(telescopeService.getUserPhotoByUri(uri)).thenReturn(someString());

        assertThat( restTemplate.exchange(
                "/api/user/telescope/photo?uri=" + uri, HttpMethod.GET, null, String.class
        )).satisfies(AssertionUtils::hasStatusOk);

        verify(telescopeService, times(1)).getUserPhotoByUri(uri);
    }

    @Test
    public void getUserPhotoByMissingUri() {
        assertThat( restTemplate.exchange(
                "/api/user/telescope/photo", HttpMethod.GET, null, String.class
        )).satisfies(AssertionUtils::hasStatusBadRequest);

        verifyZeroInteractions(telescopeService);
    }

    @Test
    public void getUserPhotoByEmptyUri() {
        assertThat( restTemplate.exchange(
                "/api/user/telescope/photo?uri=", HttpMethod.GET, null, String.class
        )).satisfies(AssertionUtils::hasStatusInternalServerError);

        verifyZeroInteractions(telescopeService);
    }
}