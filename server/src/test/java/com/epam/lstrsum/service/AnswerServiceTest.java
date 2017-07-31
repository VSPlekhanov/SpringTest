package com.epam.lstrsum.service;

import com.epam.lstrsum.SetUpDataBaseCollections;
import com.epam.lstrsum.converter.AnswerDtoConverter;
import com.epam.lstrsum.dto.answer.AnswerAllFieldsDto;
import com.epam.lstrsum.dto.answer.AnswerPostDto;
import com.epam.lstrsum.exception.AnswerValidationException;
import com.epam.lstrsum.model.Answer;
import com.epam.lstrsum.model.Request;
import com.epam.lstrsum.model.User;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.time.Instant;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class AnswerServiceTest extends SetUpDataBaseCollections {
    @Autowired
    private AnswerService answerService;

    @Autowired
    private AnswerDtoConverter answerDtoConverter;

    private final String authorEmail = "Bob_Hoplins@epam.com";

    @Test
    public void addNewAnswerWithExistingRequestTest() throws Exception {
        AnswerPostDto postDto = new AnswerPostDto("1u_2r", "answer text");

        AnswerAllFieldsDto answer = answerService.addNewAnswer(postDto, authorEmail);
        assertThat(answer.getParentId(), notNullValue());
    }

    @Test
    public void checkAnswerDtoConverterInvocation() {
        Answer someAnswer = createAnswer();
        AnswerAllFieldsDto expected = answerDtoConverter.modelToAllFieldsDto(someAnswer);

        assertThat(answerService.answerToDto(someAnswer), equalTo(expected));

    }

    private Answer createAnswer() {
        return new Answer("answerId", createRequest(),"text",
                Instant.now(), createUser(),2);
    }

    private Request createRequest() {
        return new Request("requestId", "title", new String[]{},
                "text", Instant.now(), Instant.now(),
                createUser(), Collections.emptyList(), 2);
    }

    private User createUser() {
        return new User("userId", "firstName", "lastName",
                "email", new String[]{}, Instant.now(), false);
    }

    private AnswerAllFieldsDto nonNullAnswerAllFieldsDto() {
        return new AnswerAllFieldsDto(null, null, null, null, null , null);
    }

    @Test(expected = AnswerValidationException.class)
    public void addNewAnswerNoDTOTest() throws IOException {
        answerService.addNewAnswer(null, authorEmail);
    }

    @Test(expected = AnswerValidationException.class)
    public void addNewAnswerNoAuthorTest() throws IOException {
        AnswerPostDto postDto = new AnswerPostDto("1u_2r", "answer text");
        answerService.addNewAnswer(postDto, null);
    }

    @Test(expected = AnswerValidationException.class)
    public void addNewAnswerWithNoExistingUserTest() throws IOException {
        AnswerPostDto postDto = new AnswerPostDto("1u_2r", "answer text");
        answerService.addNewAnswer(postDto, "someone_nonexisting@epam.com");
    }

    @Test(expected = AnswerValidationException.class)
    public void addNewAnswerWithNoExistingRequestIDTest() throws IOException {
        AnswerPostDto postDto = new AnswerPostDto("1s_2r", "answer text");
        answerService.addNewAnswer(postDto, "John_Doe@epam.com");
    }

}