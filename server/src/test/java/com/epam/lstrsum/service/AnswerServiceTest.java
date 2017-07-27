package com.epam.lstrsum.service;

import com.epam.lstrsum.SetUpDataBaseCollections;
import com.epam.lstrsum.dto.answer.AnswerPostDto;
import com.epam.lstrsum.exception.AnswerValidationException;
import com.epam.lstrsum.model.Answer;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class AnswerServiceTest extends SetUpDataBaseCollections {
    @Autowired
    private AnswerService answerService;

    private final String authorEmail = "Bob_Hoplins@epam.com";


    @Test
    public void addNewAnswerWithExistingRequestTest() throws Exception {
        AnswerPostDto postDto = new AnswerPostDto("1u_2r", "answer text");

        Answer answer = answerService.addNewAnswer(postDto, authorEmail);
        assertThat(answer.getParentId(), notNullValue());
    }

    @Test(expected = AnswerValidationException.class)
    public void addNewAnswerNoDTOTest() throws IOException {
        answerService.addNewAnswer(null, authorEmail);
    }

    @Test(expected = AnswerValidationException.class)
    public void addNewAnswerNoAuthorTest() throws IOException {
        AnswerPostDto postDto = new AnswerPostDto("1u_2r", "answer text");
        answerService.addNewAnswer(postDto,null);
    }

    @Test(expected = AnswerValidationException.class)
    public void addNewAnswerWithNoExistingUserTest() throws IOException {
        AnswerPostDto postDto = new AnswerPostDto("1u_2r", "answer text");
        answerService.addNewAnswer(postDto,"someone_nonexisting@epam.com");
    }

    @Test(expected = AnswerValidationException.class)
    public void addNewAnswerWithNoExistingRequestIDTest() throws IOException {
        AnswerPostDto postDto = new AnswerPostDto("1s_2r", "answer text");
        answerService.addNewAnswer(postDto,"John_Doe@epam.com");
    }

}