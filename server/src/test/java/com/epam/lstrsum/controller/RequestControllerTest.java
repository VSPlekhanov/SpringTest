package com.epam.lstrsum.controller;

import com.epam.lstrsum.dto.answer.AnswerBaseDto;
import com.epam.lstrsum.dto.request.RequestAllFieldsDto;
import com.epam.lstrsum.dto.request.RequestAppearanceDto;
import com.epam.lstrsum.dto.request.RequestBaseDto;
import com.epam.lstrsum.dto.request.RequestPostDto;
import com.epam.lstrsum.dto.user.UserBaseDto;
import com.epam.lstrsum.service.RequestService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
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
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class RequestControllerTest {

    @Mock
    private RequestService requestService;

    @Mock
    private UserRuntimeRequestComponent userRuntimeRequestComponent;

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
                1501140060439L, Collections.singletonList("Bob_Hoplins@epam.com"));
        when(userRuntimeRequestComponent.getEmail()).thenReturn("John_Doe@epam.com");

        String requestId = "Id11";
        RequestAllFieldsDto dtoWithId = new RequestAllFieldsDto(requestId, null, null, null, null
                , null, null, null, null);
        when(requestService.addNewRequest(postDto, authorEmail)).thenReturn(dtoWithId);

        controller.addRequest(postDto);

        verify(requestService).addNewRequest(postDto, authorEmail);
    }

    @Test
    public void addRequestReturnValidResponseEntityTest() throws IOException {
        String authorEmail = "John_Doe@epam.com";
        RequestPostDto postDto = new RequestPostDto("some title", new String[]{"1", "2", "3", "4"}, "some txet",
                1501145960400L, Collections.singletonList("Bob_Hoplins@epam.com"));

        String requestId = "Id11";
        RequestAllFieldsDto dtoWithId = new RequestAllFieldsDto(requestId, null, null, null, null
                , null, null, null, null);
        when(requestService.addNewRequest(postDto, authorEmail)).thenReturn(dtoWithId);

        when(userRuntimeRequestComponent.getEmail()).thenReturn("John_Doe@epam.com");
        ResponseEntity<String> actualEntity = controller.addRequest(postDto);

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

    @Test
    public void getRequestWithAnswersShouldReturnValidResponseEntityWhenRequestExists() throws Exception {
        String requestId = "requestId";

        RequestAppearanceDto requestAppearanceDto = new RequestAppearanceDto(
                requestId, "requestTitle", new String[]{"tag1", "tag2", "tag3"},
                Instant.now(), Instant.now(),
                new UserBaseDto("userId", "userName", "userSurname", "user@epam.com"),
                2, "question body",
                Arrays.asList(new AnswerBaseDto("answer1Text", Instant.now(),
                                new UserBaseDto("user1Id", "user1Name", "user1Surname", "user1@epam.com"), 6),
                        new AnswerBaseDto("answer2Text", Instant.now(),
                                new UserBaseDto("user2Id", "user2Name", "user2Surname", "user2@epam.com"), 3)));

        when(requestService.contains(requestId)).thenReturn(true);
        when(requestService.getRequestAppearanceDtoByRequestId(requestId)).thenReturn(requestAppearanceDto);

        ResponseEntity actual = controller.getRequestWithAnswers(requestId);
        ResponseEntity expected = ResponseEntity.ok(requestAppearanceDto);

        assertThat(actual, is(equalTo(expected)));
    }

    @Test
    public void getRequestWithAnswersShouldReturnNotFoundResponseEntityWhenSuchRequestDoesNotExist() throws Exception {
        String requestId = "thisRequestDoesNotExistInDb";

        when(requestService.contains(requestId)).thenReturn(false);

        ResponseEntity actual = controller.getRequestWithAnswers(requestId);
        ResponseEntity expected = new ResponseEntity(HttpStatus.NOT_FOUND);

        assertThat(actual, is(equalTo(expected)));
    }
}