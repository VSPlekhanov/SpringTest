package com.epam.lstrsum.service;

import com.epam.lstrsum.SetUpDataBaseCollections;
import com.epam.lstrsum.converter.RequestDtoConverter;
import com.epam.lstrsum.dto.answer.AnswerBaseDto;
import com.epam.lstrsum.dto.request.RequestAllFieldsDto;
import com.epam.lstrsum.dto.request.RequestAppearanceDto;
import com.epam.lstrsum.dto.request.RequestBaseDto;
import com.epam.lstrsum.dto.request.RequestPostDto;
import com.epam.lstrsum.exception.RequestValidationException;
import com.epam.lstrsum.model.Request;
import com.epam.lstrsum.persistence.RequestRepository;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;

@FixMethodOrder
public class RequestServiceTest extends SetUpDataBaseCollections {

    @Autowired
    private RequestDtoConverter requestDtoConverter;

    @Autowired
    private RequestService requestService;

    @Autowired
    private RequestRepository requestRepository;

    @Test
    public void findAllReturnsCorrectValuesTest() {
        List<Request> requestList = requestRepository.findAll();
        List<RequestAllFieldsDto> expectedAllFieldsDto = new ArrayList<>();
        for (Request request : requestList) {
            expectedAllFieldsDto.add(requestDtoConverter.modelToAllFieldsDto(request));
        }
        List<RequestAllFieldsDto> actualList = requestService.findAll();

        assertEquals(expectedAllFieldsDto, actualList);
    }

    @Test
    public void searchReturnsCorrectValue() {
        String searchPhrase = "android";
        List<Request> requestList = requestRepository.search(searchPhrase);

        List<RequestAllFieldsDto> expectedAllFieldsDto = new ArrayList<>();
        for (Request request : requestList) {
            expectedAllFieldsDto.add(requestDtoConverter.modelToAllFieldsDto(request));
        }

        List<RequestAllFieldsDto> actualList = requestService.search(searchPhrase);

        assertEquals(expectedAllFieldsDto, actualList);
        //assertThat(expectedAllFieldsDto.size(), greaterThan(0));
    }

    @Test
    public void getRequestReturnsCorrectDtoObject() {
        Request request = requestRepository.findOne("1u_1r");
        RequestAllFieldsDto dtoRequestFromRepo = requestDtoConverter.modelToAllFieldsDto(request);
        RequestAllFieldsDto dtoRequestFromService = requestService.getRequestAllFieldDtoByRequestId("1u_1r");
        assertThat(dtoRequestFromRepo, is(equalTo(dtoRequestFromService)));
    }

    @Test
    public void findAllRequestsBaseDtoReturnsCorrectValuesTest() {
        int requestsCount = (int) requestRepository.count();
        List<Request> requestList = requestRepository.findAllByOrderByCreatedAtDesc();
        List<RequestBaseDto> expectedDtoList = new ArrayList<>();
        for (Request request : requestList) {
            expectedDtoList.add(requestDtoConverter.modelToBaseDto(request));
        }
        List<RequestBaseDto> actualList = requestService.findAllRequestsBaseDto(0, requestsCount);
        assertThat(actualList.equals(expectedDtoList), is(true));
    }

    @Test
    public void addNewRequestConvertsPostDtoToRequestAndPutsItIntoDbTest() {
        RequestPostDto postDto = new RequestPostDto("this the end", new String[]{"1", "2", "3", "go"},
                "just some text", 1501145960439L,
                Arrays.asList("Bob_Hoplins@epam.com", "Tyler_Greeds@epam.com",
                        "Donald_Gardner@epam.com", "Ernest_Hemingway@epam.com"));
        String authorEmail = "John_Doe@epam.com";
        String newRequestId = requestService.addNewRequest(postDto, authorEmail).getRequestId();
        assertThat(requestService.contains(newRequestId), is(true));
    }

    @Test
    public void requestServiceIsAbleToGetRequestAppearanceDTOFromDBIfIdIsValid() {
        RequestAppearanceDto dtoRequestDto = requestService.getRequestAppearanceDtoByRequestId("1u_1r");

        assertThat(dtoRequestDto == null, is(false));
    }

    @Test
    public void requestServiceIsAbleToGetRequestWithAnswersFromDBIfRequestHasThem() {
        RequestAppearanceDto dtoRequestDtoWithAnswers = requestService.getRequestAppearanceDtoByRequestId("1u_1r");

        assertThat(dtoRequestDtoWithAnswers.getAnswers().isEmpty(), is(false));
    }

    @Test
    public void requestServiceIsAbleToGetRequestWithoutAnswersFromDB() {
        RequestAppearanceDto dtoRequestDtoWithoutAnswers = requestService.getRequestAppearanceDtoByRequestId("6u_6r");

        assertThat(dtoRequestDtoWithoutAnswers.getAnswers().isEmpty(), is(true));
        assertThat(dtoRequestDtoWithoutAnswers.getAnswers() == null, is(false));
    }

    @Test
    public void requestServiceReturnsListOfRequestAnswersInCorrectAscOrder() {
        RequestAppearanceDto dtoRequestDtoWithAnswers = requestService.getRequestAppearanceDtoByRequestId("1u_1r");
        List<AnswerBaseDto> requestAnswers = dtoRequestDtoWithAnswers.getAnswers();

        for (int i = 1; i < requestAnswers.size(); i++) {
            assertThat(requestAnswers.get(i - 1).getCreatedAt().
                    isBefore(requestAnswers.get(i).getCreatedAt()), is(true));
        }
    }

    @Test(expected = RequestValidationException.class)
    public void requestServiceThrowsRequestValidationExceptionForNullTitleInPostDto() {
        RequestPostDto postDto = new RequestPostDto(null, new String[]{"1", "2", "3", "go"},
                "just some text", 1501112360439L,
                Arrays.asList("Bob_Hoplins@epam.com", "Tyler_Greeds@epam.com",
                        "Donald_Gardner@epam.com", "Ernest_Hemingway@epam.com"));
        String authorEmail = "John_Doe@epam.com";
        requestService.addNewRequest(postDto, authorEmail);
    }

    @Test(expected = RequestValidationException.class)
    public void requestServiceThrowsRequestValidationExceptionForNullTextInPostDtoTest() {
        RequestPostDto postDto = new RequestPostDto("just some title", new String[]{"1", "2", "3", "go"},
                null, 1501145111439L,
                Arrays.asList("Bob_Hoplins@epam.com", "Tyler_Greeds@epam.com",
                        "Donald_Gardner@epam.com", "Ernest_Hemingway@epam.com"));
        String authorEmail = "John_Doe@epam.com";
        requestService.addNewRequest(postDto, authorEmail);
    }

    @Test(expected = RequestValidationException.class)
    public void requestServiceThrowsRequestValidationExceptionIfTextIsTooShortInPostDtoTest() {
        RequestPostDto postDto = new RequestPostDto("just some title", new String[]{"1", "2", "3", "go"},
                "", 1501145922239L,
                Arrays.asList("Bob_Hoplins@epam.com", "Tyler_Greeds@epam.com",
                        "Donald_Gardner@epam.com", "Ernest_Hemingway@epam.com"));
        String authorEmail = "John_Doe@epam.com";
        requestService.addNewRequest(postDto, authorEmail);
    }

    @Test(expected = RequestValidationException.class)
    public void requestServiceThrowsRequestValidationExceptionIfTitleIsTooShortInPostDtoTest() {
        RequestPostDto postDto = new RequestPostDto("tle", new String[]{"1", "2", "3", "go"},
                "just some text", 1501143330439L,
                Arrays.asList("Bob_Hoplins@epam.com", "Tyler_Greeds@epam.com",
                        "Donald_Gardner@epam.com", "Ernest_Hemingway@epam.com"));
        String authorEmail = "John_Doe@epam.com";
        requestService.addNewRequest(postDto, authorEmail);
    }

    @Test(expected = RequestValidationException.class)
    public void addNewRequestThrowsRequestValidationExceptionIfRequestPostDtoIsNullTest() {
        String authorEmail = "John_Doe@epam.com";
        requestService.addNewRequest(null, authorEmail);
    }

    @Test(expected = RequestValidationException.class)
    public void addNewRequestThrowsRequestValidationExceptionIfRequestAuthorEmailIsNullTest() {
        requestService.addNewRequest(new RequestPostDto("just some title", new String[]{"1", "2", "3", "go"},
                "just some text", 1501144323239L,
                Arrays.asList("Bob_Hoplins@epam.com", "Tyler_Greeds@epam.com",
                        "Donald_Gardner@epam.com", "Ernest_Hemingway@epam.com")), null);
    }
}