package com.epam.lstrsum.converter;


import com.epam.lstrsum.SetUpDataBaseCollections;
import com.epam.lstrsum.dto.request.RequestAllFieldsDto;
import com.epam.lstrsum.dto.request.RequestPostDto;
import com.epam.lstrsum.dto.user.UserBaseDto;
import com.epam.lstrsum.model.Request;
import com.epam.lstrsum.model.User;
import com.epam.lstrsum.persistence.RequestRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;

public class RequestDtoConverterTest extends SetUpDataBaseCollections {

    @Autowired
    private RequestDtoConverter requestConverter;

    @Autowired
    private UserDtoConverter userConverter;

    @Autowired
    private RequestRepository requestRepository;

    @Test
    public void convertFromPostDtoToRequestReturnsExpectedValue() {
        RequestPostDto postDto = new RequestPostDto("this the end", new String[]{"1", "2", "3", "go"},
                "just some text", "2017-11-29T10:15:30Z",
                Arrays.asList("Bob_Hoplins@epam.com", "Tyler_Greeds@epam.com",
                        "Donald_Gardner@epam.com", "Ernest_Hemingway@epam.com"));
        String authorEmail = "John_Doe@epam.com";
        Request convertedRequest = requestConverter.requestPostDtoAndAuthorEmailToRequest(postDto, authorEmail);
        assertThat(postDto.getTitle(), is(equalTo(convertedRequest.getTitle())));
        assertThat(postDto.getTags(), is(equalTo(convertedRequest.getTags())));
        assertThat(Instant.parse(postDto.getDeadLine()), is(equalTo(convertedRequest.getDeadLine())));
        assertThat(postDto.getText(), is(equalTo(convertedRequest.getText())));
        List<String> subsFromRequest = convertedRequest.getAllowedSubs().stream().map(User::getEmail).collect(Collectors.toList());
        assertThat(postDto.getAllowedSubs(), is(equalTo(subsFromRequest)));

    }

    @Test
    public void convertRequestToDtoReturnsExpectedValue() {
        Request request = requestRepository.findOne("1u_1r");
        RequestAllFieldsDto allFieldsDto = requestConverter.modelToAllFieldsDto(request);

        List<UserBaseDto> userBaseDtos = userConverter.allowedSubsToListOfUserBaseDtos(request.getAllowedSubs());

        assertThat(request.getRequestId(), is(equalTo(allFieldsDto.getRequestId())));
        assertThat(request.getTitle(), is(equalTo(allFieldsDto.getTitle())));
        assertThat(request.getTags(), is(equalTo(allFieldsDto.getTags())));
        assertThat(request.getText(), is(equalTo(allFieldsDto.getText())));
        assertThat(request.getCreatedAt(), is(equalTo(allFieldsDto.getCreatedAt())));
        assertThat(request.getDeadLine(), is(equalTo(allFieldsDto.getDeadLine())));
        assertThat(userConverter.modelToBaseDto(request.getAuthorId()), is(equalTo(allFieldsDto.getAuthor())));
        assertThat(userBaseDtos, is(equalTo(allFieldsDto.getAllowedSubs())));
        assertThat(request.getUpVote(), is(equalTo(allFieldsDto.getUpVote())));
    }


    @Test
    public void converterIsAbleToCreateRequestWithEmptySubList() {
        RequestPostDto postDto = new RequestPostDto("this the end", new String[]{"1", "2", "3", "go"},
                "just some text", "2017-11-29T10:15:30Z",
                Collections.emptyList());
        String authorEmail = "John_Doe@epam.com";
        Request request = requestConverter.requestPostDtoAndAuthorEmailToRequest(postDto, authorEmail);
        assertThat(request, notNullValue());
    }

    @Test
    public void converterIsAbleToCreateRequestWithEmptyTagsArray() {
        RequestPostDto postDto = new RequestPostDto("this the end", new String[0],
                "just some text", "2017-11-29T10:15:30Z",
                Collections.emptyList());
        String authorEmail = "John_Doe@epam.com";
        Request request = requestConverter.requestPostDtoAndAuthorEmailToRequest(postDto, authorEmail);
        assertThat(request, notNullValue());
    }

}
