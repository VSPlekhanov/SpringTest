package com.epam.lstrsum.controller;

import com.epam.lstrsum.dto.request.RequestBaseDto;
import com.epam.lstrsum.dto.request.RequestPostDto;
import com.epam.lstrsum.dto.user.UserBaseDto;
import com.epam.lstrsum.service.RequestService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class RequestControllerTest {

    @Mock
    private RequestService requestService;

    @InjectMocks
    private RequestController controller;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    @Test
    public void addRequestShouldSaveRequestTest() throws IOException {
        String authorEmail = "John_Doe@epam.com";
        RequestPostDto postDto = new RequestPostDto("some title", new String[]{"1", "2", "3", "4"}, "some txet",
                "2017-11-29T10:15:30Z", Collections.singletonList("Bob_Hoplins@epam.com"));
        controller.addRequest(null, postDto);

        verify(requestService).addNewRequest(postDto, authorEmail);
    }

    @Test
    public void addRequestReturnValidResponseEntityTest() throws IOException {
        String authorEmail = "John_Doe@epam.com";
        RequestPostDto postDto = new RequestPostDto("some title", new String[]{"1", "2", "3", "4"}, "some txet",
                "2017-11-29T10:15:30Z", Collections.singletonList("Bob_Hoplins@epam.com"));
        String requestId = "Id11";
        when(requestService.addNewRequest(postDto, authorEmail)).thenReturn(requestId);
        ResponseEntity<String> actualEntity = controller.addRequest(null, postDto);

        ResponseEntity<String> expectedEntity = ResponseEntity.ok(requestId);

        assertThat(actualEntity, is(equalTo(expectedEntity)));
    }

    @Test
    public void getRequestsReturnsValidResponseEntityTest() {
        int requestAmount = 15;
        int requestPage = 4;
        controller.setMaxRequestAmount(requestAmount);
        List<RequestBaseDto> list = Arrays.asList(new RequestBaseDto("u1", "some title 2", null,
                        Instant.now(), Instant.now(),
                        new UserBaseDto("some user id 2", "first name", "last name", "some@email.com"),
                        1),
                new RequestBaseDto("u2", "some title 2", null,
                        Instant.now(), Instant.now(),
                        new UserBaseDto("some user id 2", "first name", "last name", "some@email.com"),
                        1));
        when(requestService.findAllRequestsBaseDto(requestPage, requestAmount)).thenReturn(list);
        ResponseEntity<List<RequestBaseDto>> actualEntity = controller.getRequests(requestPage, requestAmount);
        ResponseEntity<List<RequestBaseDto>> expectedEntity = ResponseEntity.ok(list);
        assertThat(actualEntity, is(equalTo(expectedEntity)));
    }

    @Test
    public void getRequestsParamsCantLessThenZeroTest() {
        int maxRequestAmount = 15;
        int minRequestPage = 0;

        int requestAmount = -5;
        int requestPage = -4;
        controller.setMaxRequestAmount(maxRequestAmount);
        List list = new ArrayList();
        when(requestService.findAllRequestsBaseDto(minRequestPage, maxRequestAmount)).thenReturn(list);

        ResponseEntity<List<RequestBaseDto>> actualEntity = controller.getRequests(requestPage, requestAmount);
        ResponseEntity<List<RequestBaseDto>> expectedEntity = ResponseEntity.ok(list);

        assertThat(actualEntity, is(equalTo(expectedEntity)));
    }
}