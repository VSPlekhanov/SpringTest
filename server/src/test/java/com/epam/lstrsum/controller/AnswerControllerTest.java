package com.epam.lstrsum.controller;

import com.epam.lstrsum.SetUpDataBaseCollections;
import com.epam.lstrsum.dto.answer.AnswerAllFieldsDto;
import com.epam.lstrsum.dto.answer.AnswerPostDto;
import com.epam.lstrsum.dto.request.RequestBaseDto;
import com.epam.lstrsum.dto.request.RequestPostDto;
import com.epam.lstrsum.dto.user.UserBaseDto;
import com.epam.lstrsum.exception.AnswerValidationException;
import com.epam.lstrsum.model.Answer;
import com.epam.lstrsum.model.Request;
import com.epam.lstrsum.model.User;
import com.epam.lstrsum.service.AnswerService;
import com.epam.lstrsum.service.RequestService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.time.Instant;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class AnswerControllerTest extends SetUpDataBaseCollections {

    @InjectMocks
    private AnswerController answerController;

    @Mock
    private AnswerService answerService;


    private String requestId = "1u_3r";
    private AnswerPostDto answerPostDto;
    private AnswerAllFieldsDto answerAllFieldsDto;
    private Answer answer;
    private ResponseEntity<AnswerAllFieldsDto> responseEntity;
    private String email = "John_Doe@epam.com";

    @Before
    public void setUp() {
        initMocks(this);
        answerPostDto = new AnswerPostDto(requestId, "text");
        answerAllFieldsDto = new AnswerAllFieldsDto(
                "text", Instant.now(),
                new UserBaseDto(
                        "userId1",
                        "John",
                        "Doe",
                        email),
                0, "answerId1",
                new RequestBaseDto(
                        requestId,
                        "title",
                        new String[]{"tag1", "tag2"},
                        Instant.now(),
                        Instant.now(),
                        new UserBaseDto(
                                "userId2",
                                "user2firstName",
                                "lastName",
                                "user2firstName_lastName@epam.com"),
                        0));
        answer = new Answer(
                "answerId1",
                new Request(
                        requestId,
                        "title",
                        new String[]{"tag1", "tag2"},
                        "text",
                        Instant.now(),
                        Instant.now(),
                        new User(
                                "userId2",
                                "user2firstName",
                                "lastName",
                                "user2firstName_lastName@epam.com",
                                new String[]{"allowed"},
                                Instant.now(),
                                true),
                        Collections.EMPTY_LIST,
                        0),
                "text",
                Instant.now(),
                new User(
                        "userId1",
                        "John",
                        "Doe",
                        email,
                        new String[]{"allowed"},
                        Instant.now(),
                        true),
                0);
    }

    @Test
    public void addNewAnswerTest() throws IOException {
        when(answerService.addNewAnswer(answerPostDto, email)).thenReturn(answer);
        when(answerService.answerToDto(answer)).thenReturn(answerAllFieldsDto);
        responseEntity = answerController.addAnswer(null, answerPostDto);
        verify(answerService).addNewAnswer(answerPostDto, email);
        assertThat(responseEntity, notNullValue());
        assertThat(responseEntity.getBody(), notNullValue());
        assertThat(responseEntity.getBody().getAuthorId(), notNullValue());
        assertThat(responseEntity.getBody().getParentId(), notNullValue());
        assertThat(responseEntity.getBody().getCreatedAt(), notNullValue());
        assertThat(responseEntity.getBody().getText(), is("text"));
        assertThat(responseEntity.getBody().getUpVote(), is(0));
    }

    @Test(expected = AnswerValidationException.class)
    //TODO Test purposes only
    public void addNewAnswerNoRequestTest() throws IOException {
        answerPostDto = new AnswerPostDto(null, "text");
        when(answerService.addNewAnswer(answerPostDto, email)).thenThrow(AnswerValidationException.class);
        responseEntity = answerController.addAnswer(null, answerPostDto);
    }

    @Test(expected = AnswerValidationException.class)
    //TODO Test purposes only
    public void addNewAnswerWithNoTextTest() throws IOException {
        answerPostDto = new AnswerPostDto(requestId, null);
        when(answerService.addNewAnswer(answerPostDto, email)).thenThrow(AnswerValidationException.class);
        responseEntity = answerController.addAnswer(null, answerPostDto);
    }


    @Test
    public void addNewAnswerNoAuthorTest() throws IOException {
        responseEntity = answerController.addAnswer(null, answerPostDto);
    }

    @Test(expected = AnswerValidationException.class)
    public void addNewAnswerNoDTOTest() throws IOException {
        when(answerService.addNewAnswer(null, email)).thenThrow(AnswerValidationException.class);
        responseEntity = answerController.addAnswer(null, null);
    }




//    @Ignore
//    @Test
//    public void getListOfAnswers() throws Exception {
//        final ResponseEntity<List<Answer>> responseEntity = testRestTemplate.exchange("/api/answer",
//                HttpMethod.GET, null, new ParameterizedTypeReference<List<Answer>>() {
//                });
//        List<Answer> actualList = responseEntity.getBody();
//        //validate
//        assertThat(actualList.size(), is(12));
//        List<String> actualIds = actualList.stream().map(Answer::getAnswerId).collect(collectingAndThen(toList(), ImmutableList::copyOf));
//        assertThat(actualIds, containsInAnyOrder("1u_1r_1a", "1u_1r_2a", "1u_1r_3a", "1u_2r_1a", "1u_2r_2a",
//                "2u_3r_1a", "2u_3r_2a", "3u_4r_1a", "3u_4r_2a", "4u_5r_1a", "4u_5r_2a", "4u_5r_3a"));
//    }
}