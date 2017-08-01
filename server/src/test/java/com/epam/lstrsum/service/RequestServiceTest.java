package com.epam.lstrsum.service;

import com.epam.lstrsum.SetUpDataBaseCollections;
import com.epam.lstrsum.converter.RequestDtoConverter;
import com.epam.lstrsum.dto.answer.AnswerBaseDto;
import com.epam.lstrsum.dto.request.RequestAllFieldsDto;
import com.epam.lstrsum.dto.request.RequestAppearanceDto;
import com.epam.lstrsum.dto.request.RequestBaseDto;
import com.epam.lstrsum.dto.request.RequestPostDto;
import com.epam.lstrsum.dto.user.UserBaseDto;
import com.epam.lstrsum.exception.RequestValidationException;
import com.epam.lstrsum.model.Request;
import com.epam.lstrsum.persistence.RequestRepository;
import org.assertj.core.api.Assertions;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;

@FixMethodOrder
public class RequestServiceTest extends SetUpDataBaseCollections {
    private static final String SEARCH_PHRASE = "android";
    private static final int PAGE_SIZE = 1;
    private static final int START_PAGE = 0;
    private static final int NONEXISTENT_PAGE = 2;
    private static final RequestAllFieldsDto REQUEST_WITH_ANDROID_TEXT = new RequestAllFieldsDto(
            "1u_1r",
            "JsonMappingException on android spring httprequest",
            new String[]{"java", "android", "json", "spring"},
            LocalDateTime.parse("2016-04-19T11:00:00").toInstant(ZoneOffset.UTC),
            LocalDateTime.parse("2016-05-19T11:00:00").toInstant(ZoneOffset.UTC),
            new UserBaseDto(
                    "5u", "Ernest", "Hemingway", "Ernest_Hemingway@epam.com"
            ),
            0,
            Arrays.asList(
                    new UserBaseDto("2u", "Bob", "Hoplins", "Bob_Hoplins@epam.com"),
                    new UserBaseDto("3u", "Tyler", "Greeds", "Tyler_Greeds@epam.com"),
                    new UserBaseDto("4u", "Donald", "Gardner", "Donald_Gardner@epam.com"),
                    new UserBaseDto("5u", "Ernest", "Hemingway", "Ernest_Hemingway@epam.com"),
                    new UserBaseDto("6u", "Steven", "Tyler", "Steven_Tyler@epam.com")
            ),
            "I have this call in async task. All parameters are correct. In postman or advance rest client the call work fine and It return a json with a list of objects. But if I try to do this call in android with spring I have this error:"
    );

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
    public void searchReturnsEmptyListFromNonexistentPage() {
        Assertions.assertThat(requestService.search(SEARCH_PHRASE, NONEXISTENT_PAGE, PAGE_SIZE))
                .hasSize(0);
    }

    @Test
    public void searchReturnsCorrectValueFromStartOfList() {
        List<RequestAllFieldsDto> actualList = requestService.search(SEARCH_PHRASE, START_PAGE, PAGE_SIZE);

        assertThatListHasRightSizeAndContainsCorrectValue(actualList, 1, REQUEST_WITH_ANDROID_TEXT, SEARCH_PHRASE);
    }

    @Test
    public void searchWithNullSize() {
        List<RequestAllFieldsDto> actualList = requestService.search(SEARCH_PHRASE, START_PAGE, null);

        assertThatListHasRightSizeAndContainsCorrectValue(actualList, 2, REQUEST_WITH_ANDROID_TEXT, SEARCH_PHRASE);
    }

    @Test
    public void searchWithNegativeSize() {
        List<RequestAllFieldsDto> actualList = requestService.search(SEARCH_PHRASE, START_PAGE, -5);

        assertThatListHasRightSizeAndContainsCorrectValue(actualList, 2, REQUEST_WITH_ANDROID_TEXT, SEARCH_PHRASE);
    }

    @Test
    public void searchWithTooBigPageSize() {
        List<RequestAllFieldsDto> actualList = requestService.search(SEARCH_PHRASE, START_PAGE, 100000);

        assertThatListHasRightSizeAndContainsCorrectValue(actualList, 2, REQUEST_WITH_ANDROID_TEXT, SEARCH_PHRASE);
    }

    @Test
    public void searchWithNegativeStartPage() {
        List<RequestAllFieldsDto> actualList = requestService.search(SEARCH_PHRASE, -1, PAGE_SIZE);

        assertThatListHasRightSizeAndContainsCorrectValue(actualList, 1, REQUEST_WITH_ANDROID_TEXT, SEARCH_PHRASE);
    }

    @Test
    public void getTextSearchResultsCountCorrect() {
        int expected = requestRepository.getTextSearchResultsCount("android");
        Integer actual = requestService.getTextSearchResultsCount("android");

        assertThat(actual, is(expected));
    }

    private void assertThatListHasRightSizeAndContainsCorrectValue(
            List<RequestAllFieldsDto> actualList,
            int size,
            RequestAllFieldsDto oneOfRequests,
            String searchPhrase
    ) {
        Assertions.assertThat(actualList)
                .hasSize(size)
                .contains(oneOfRequests);
        assertThat(actualList.isEmpty(), is(false));
        Assertions.assertThat(actualList)
                .allMatch(requestAllFieldsDto -> requestAllFieldsDto.getText().contains(searchPhrase));
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