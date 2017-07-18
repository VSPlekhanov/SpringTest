package com.epam.lstrsum.controller;


import com.epam.lstrsum.converter.ModelDtoConverter;
import com.epam.lstrsum.dto.RequestAllFieldsDto;
import com.epam.lstrsum.dto.RequestPostDto;
import com.epam.lstrsum.model.Request;
import com.epam.lstrsum.model.User;
import com.epam.lstrsum.persistence.RequestRepository;
import com.epam.lstrsum.service.RequestService;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;

@Ignore
@RunWith(SpringRunner.class)
@SpringBootTest
public class ModelDtoConverterTest extends SetUpDataBaseCollections {

    @Autowired
    private ModelDtoConverter modelDtoConverter;

    @Autowired
    private RequestService requestService;

    @Autowired
    private RequestRepository requestRepository;

    @Test
    public void convertFromPostDtoToRequestReturnsExpectedValue() {
        RequestPostDto postDto = new RequestPostDto("this the end", new String[]{"1", "2", "3", "go"},
                "just some text", "2017-11-29T10:15:30Z",
                Arrays.asList("Bob_Hoplins@epam.com", "Tyler_Greeds@epam.com",
                        "Donald_Gardner@epam.com", "Ernest_Hemingway@epam.com"));
        String authorEmail = "John_Doe@epam.com";
        Request convertedRequest = modelDtoConverter.requestDtoAndAuthorEmailToRequest(postDto, authorEmail);
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
        RequestAllFieldsDto allFieldsDto = modelDtoConverter.requestToAllFieldsDto(request);
        assertThat(request.getRequestId(), is(equalTo(allFieldsDto.getRequestId())));
        assertThat(request.getTitle(), is(equalTo(allFieldsDto.getTitle())));
        assertThat(request.getTags(), is(equalTo(allFieldsDto.getTags())));
        assertThat(request.getText(), is(equalTo(allFieldsDto.getText())));
        assertThat(request.getCreatedAt(), is(equalTo(allFieldsDto.getCreatedAt())));
        assertThat(request.getDeadLine(), is(equalTo(allFieldsDto.getDeadLine())));
        assertThat(request.getAuthorId(), is(equalTo(allFieldsDto.getAuthorId())));
        assertThat(request.getAllowedSubs(), is(equalTo(allFieldsDto.getAllowedSubs())));
        assertThat(request.getUpVote(), is(equalTo(allFieldsDto.getUpVote())));
    }

}
