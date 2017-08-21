package com.epam.lstrsum.service;

import com.epam.lstrsum.exception.BusinessLogicException;
import com.epam.lstrsum.service.http.HttpRequestService;
import com.epam.lstrsum.utils.HttpUtilEntity;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("email")
public class HttpRequestServiceTest {

    @Autowired
    private HttpRequestService httpRequestService;

    @MockBean
    private RestTemplate restTemplate;
    private ResponseEntity mockResponse;

    @Before
    public void setUp() {
        mockResponse = mock(ResponseEntity.class);

        doReturn(mockResponse).when(restTemplate)
                .exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), any(new ParameterizedTypeReference<String>() {
                }.getClass()));
    }

    @Test(expected = BusinessLogicException.class)
    public void sendGETRequestWithNullUrl() {
        final HttpUtilEntity httpUtilEntity = HttpUtilEntity.builder().url(null).build();

        httpRequestService.sendGetRequest(httpUtilEntity, String.class);
    }

    @Test
    public void sendGETRequestWithCredentialsAndHeadersAndParams() {
        final HttpUtilEntity httpUtilEntity = HttpUtilEntity.builder()
                .username("username")
                .password("password")
                .headersNames(Collections.singletonList("Cache-Control"))
                .headersValues(Collections.singletonList("private"))
                .parametersNames(Collections.singletonList("fields"))
                .parametersValues(Collections.singletonList("fullName"))
                .url("https://telescope.epam.com")
                .build();

        when(mockResponse.getStatusCode()).thenReturn(HttpStatus.OK);
        when(mockResponse.getBody()).thenReturn("body");

        assertThat(httpRequestService.sendGetRequest(httpUtilEntity, String.class), is("body"));
    }

    @Test
    public void sendGETRequestWithWrongCredentials() {
        final HttpUtilEntity httpUtilEntity = HttpUtilEntity.builder()
                .username("incorrect")
                .password("password")
                .build();

        when(mockResponse.getStatusCode()).thenReturn(HttpStatus.FORBIDDEN);

        assertThatThrownBy(() ->
                httpRequestService.sendGetRequest(httpUtilEntity, String.class)
        ).isInstanceOf(BusinessLogicException.class);
    }
}
