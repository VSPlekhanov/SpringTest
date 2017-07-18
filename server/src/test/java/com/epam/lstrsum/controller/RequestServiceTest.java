package com.epam.lstrsum.controller;


import com.epam.lstrsum.converter.ModelDtoConverter;
import com.epam.lstrsum.dto.RequestAllFieldsDto;
import com.epam.lstrsum.dto.RequestPostDto;
import com.epam.lstrsum.model.Request;
import com.epam.lstrsum.persistence.RequestRepository;
import com.epam.lstrsum.service.RequestService;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest
@FixMethodOrder
public class RequestServiceTest extends SetUpDataBaseCollections {
    @Autowired
    private ModelDtoConverter modelDtoConverter;

    @Autowired
    private RequestService requestService;

    @Autowired
    private RequestRepository requestRepository;

    @Test
    public void getRequestReturnsCorrectDtoObject() {
        Request request = requestRepository.findOne("1u_1r");
        RequestAllFieldsDto dtoRequestFromRepo = modelDtoConverter.requestToAllFieldsDto(request);
        RequestAllFieldsDto dtoRequestFromService = requestService.getRequestDtoByRequestId("1u_1r");
        assertThat(dtoRequestFromRepo, is(equalTo(dtoRequestFromService)));
    }

    @Test
    public void addNewRequestConvertsPostDtoToRequestAndPutsItIntoDb() {
        RequestPostDto postDto = new RequestPostDto("this the end", new String[]{"1", "2", "3", "go"},
                "just some text", "2017-11-29T10:15:30Z",
                Arrays.asList("Bob_Hoplins@epam.com", "Tyler_Greeds@epam.com",
                        "Donald_Gardner@epam.com", "Ernest_Hemingway@epam.com"));
        String authorEmail = "John_Doe@epam.com";
        Request newRequest = requestService.addNewRequest(postDto, authorEmail);
        RequestAllFieldsDto requestAllFieldsDto = modelDtoConverter.requestToAllFieldsDto(newRequest);
        List<RequestAllFieldsDto> requestAllFieldsDtoListFromDb = requestService.findAll();
        assertTrue(requestAllFieldsDtoListFromDb.contains(requestAllFieldsDto));
    }
}
