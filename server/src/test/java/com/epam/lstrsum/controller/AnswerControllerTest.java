package com.epam.lstrsum.controller;

import com.epam.lstrsum.SetUpDataBaseCollections;
import com.epam.lstrsum.dto.answer.AnswerAllFieldsDto;
import com.epam.lstrsum.dto.answer.AnswerBaseDto;
import com.epam.lstrsum.dto.answer.AnswerPostDto;
import com.epam.lstrsum.dto.question.QuestionBaseDto;
import com.epam.lstrsum.dto.user.UserBaseDto;
import com.epam.lstrsum.dto.vote.VoteAllFieldsDto;
import com.epam.lstrsum.exception.AnswerValidationException;
import com.epam.lstrsum.exception.NoSuchAnswerException;
import com.epam.lstrsum.exception.NoSuchUserException;
import com.epam.lstrsum.model.Answer;
import com.epam.lstrsum.model.Question;
import com.epam.lstrsum.model.User;
import com.epam.lstrsum.service.AnswerService;
import com.epam.lstrsum.service.VoteService;
import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.validation.ConstraintViolationException;
import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;


public class AnswerControllerTest extends SetUpDataBaseCollections {

    @Mock
    private AnswerService answerService;

    @Mock
    private VoteService voteService;

    @Mock
    private UserRuntimeRequestComponent userRuntimeRequestComponent;
    @Autowired
    private TestRestTemplate testRestTemplate;

    private AnswerController answerController;

    private String questionId = "1u_3r";
    private AnswerPostDto answerPostDto;
    private AnswerAllFieldsDto answerAllFieldsDto;
    private VoteAllFieldsDto voteAllFieldsDto;
    private Answer answer;
    private ResponseEntity<AnswerAllFieldsDto> responseEntity;
    private String authorEmail = "John_Doe@epam.com";

    @Before
    public void setUp() {
        initMocks(this);
        answerController = new AnswerController(answerService, voteService, userRuntimeRequestComponent);
        answerAllFieldsDto = new AnswerAllFieldsDto(
                "text", Instant.now(),
                new UserBaseDto(
                        "userId1",
                        "John",
                        "Doe",
                        authorEmail),
                0, "answerId1",
                new QuestionBaseDto(
                        questionId,
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
                Question.builder()
                        .questionId(questionId)
                        .title("title")
                        .tags(new String[]{"tag1", "tag2"})
                        .text("text")
                        .createdAt(Instant.now())
                        .deadLine(Instant.now())
                        .authorId(new User("userId2",
                                "user2firstName",
                                "lastName",
                                "user2firstName_lastName@epam.com",
                                new String[]{"allowed"},
                                Instant.now(),
                                true))
                        .allowedSubs(Collections.EMPTY_LIST)
                        .upVote(0)
                        .build(),
                "text",
                Instant.now(),
                new User(
                        "userId1",
                        "John",
                        "Doe",
                        authorEmail,
                        new String[]{"allowed"},
                        Instant.now(),
                        true),
                0);
        voteAllFieldsDto = new VoteAllFieldsDto("voteId1",
                Instant.now(),
                false,
                new AnswerBaseDto("some text",
                        Instant.now(),
                        new UserBaseDto("userId1",
                                "Bob",
                                "Hoplins",
                                "Bob_Hoplins@epam.com"),
                        0),
                new UserBaseDto("userId2",
                        "Tyler",
                        "Greeds",
                        "Tyler_Greeds@epam.com"));
    }

    @Test
    public void addNewAnswerTest() throws IOException {
        answerPostDto = new AnswerPostDto(questionId, "text");
        when(answerService.addNewAnswer(answerPostDto, authorEmail)).thenReturn(answerAllFieldsDto);
        when(userRuntimeRequestComponent.getEmail()).thenReturn("John_Doe@epam.com");
        when(answerService.answerToDto(answer)).thenReturn(answerAllFieldsDto);
        responseEntity = answerController.addAnswer(answerPostDto);
        verify(answerService).addNewAnswer(answerPostDto, authorEmail);
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
    public void addNewAnswerNoQuestionTest() throws IOException {
        answerPostDto = new AnswerPostDto(null, "text");
        when(userRuntimeRequestComponent.getEmail()).thenReturn("John_Doe@epam.com");
        when(answerService.addNewAnswer(answerPostDto, authorEmail)).thenThrow(AnswerValidationException.class);
        responseEntity = answerController.addAnswer(answerPostDto);
    }

    @Test(expected = AnswerValidationException.class)
    //TODO Test purposes only
    public void addNewAnswerWithNoTextTest() throws IOException {
        answerPostDto = new AnswerPostDto(questionId, null);
        when(userRuntimeRequestComponent.getEmail()).thenReturn("John_Doe@epam.com");
        when(answerService.addNewAnswer(answerPostDto, authorEmail)).thenThrow(AnswerValidationException.class);
        responseEntity = answerController.addAnswer(answerPostDto);
    }

    @Ignore
    @Test
    public void getListOfAnswers() throws Exception {

        final ResponseEntity<List<Answer>> responseEntity = testRestTemplate.exchange("/api/answer",
                HttpMethod.GET, null, new ParameterizedTypeReference<List<Answer>>() {
                });
        List<Answer> actualList = responseEntity.getBody();
        //validate
        assertThat(actualList.size(), is(12));
        List<String> actualIds = actualList.stream().map(Answer::getAnswerId).collect(collectingAndThen(toList(), ImmutableList::copyOf));
        assertThat(actualIds, containsInAnyOrder("1u_1r_1a", "1u_1r_2a", "1u_1r_3a", "1u_2r_1a", "1u_2r_2a",
                "2u_3r_1a", "2u_3r_2a", "3u_4r_1a", "3u_4r_2a", "4u_5r_1a", "4u_5r_2a", "4u_5r_3a"));
    }

    @Test
    public void addVote() {
        final String voterEmail = "Tyler_Greeds@epam.com";
        final String someAnswerId = "answerId1";

        when(userRuntimeRequestComponent.getEmail()).thenReturn(voterEmail);
        when(voteService.addVoteToAnswer(voterEmail, someAnswerId)).thenReturn(voteAllFieldsDto);

        ResponseEntity<VoteAllFieldsDto> responseEntity = answerController.addVote(someAnswerId);
        VoteAllFieldsDto responseDto = responseEntity.getBody();

        assertNotNull(responseDto);
        assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
        assertThat(responseDto.getVoteId(), is(voteAllFieldsDto.getVoteId()));
        assertThat(responseDto.isRevoked(), is(voteAllFieldsDto.isRevoked()));
        assertThat(responseDto.getAnswerBaseDto(), is(voteAllFieldsDto.getAnswerBaseDto()));
        assertThat(responseDto.getUserBaseDto(), is(voteAllFieldsDto.getUserBaseDto()));

        verify(userRuntimeRequestComponent, times(1)).getEmail();
        verify(voteService, times(1)).addVoteToAnswer(voterEmail, someAnswerId);
    }

    @Test(expected = ConstraintViolationException.class)
    public void addVoteWithEmptyAnswerId() {
        final String someEmail = "John_Doe@epam.com";
        final String emptyAnswerId = "        ";

        when(userRuntimeRequestComponent.getEmail()).thenReturn(someEmail);
        when(voteService.addVoteToAnswer(someEmail, emptyAnswerId)).thenThrow(ConstraintViolationException.class);
        answerController.addVote(emptyAnswerId);

        verify(userRuntimeRequestComponent, times(1)).getEmail();
        verify(voteService, times(1)).addVoteToAnswer(someEmail, emptyAnswerId);
    }

    @Test(expected = NoSuchAnswerException.class)
    public void deleteVoteWithNonExistingAnswerId() {
        final String incorrectAnswerId = "incorrectAnswerId";
        final String someEmail = "John_Doe@epam.com";

        when(userRuntimeRequestComponent.getEmail()).thenReturn(someEmail);
        when(voteService.addVoteToAnswer(someEmail, incorrectAnswerId)).thenThrow(NoSuchAnswerException.class);

        answerController.addVote(incorrectAnswerId);

        verify(userRuntimeRequestComponent, times(1)).getEmail();
        verify(voteService, times(1)).addVoteToAnswer(someEmail, incorrectAnswerId);
    }

    @Test(expected = NoSuchUserException.class)
    public void deleteVoteWithNonExistingEmail() {
        final String someAnswerId = "answerId1";
        final String nonExistingEmail = "test@test.com";

        when(userRuntimeRequestComponent.getEmail()).thenReturn(nonExistingEmail);
        when(voteService.addVoteToAnswer(nonExistingEmail, someAnswerId)).thenThrow(NoSuchUserException.class);

        answerController.addVote(someAnswerId);

        verify(userRuntimeRequestComponent, times(1)).getEmail();
        verify(voteService, times(1)).addVoteToAnswer(nonExistingEmail, someAnswerId);
    }

    @Test
    public void deleteVote() {
        final String voterEmail = "Tyler_Greeds@epam.com";
        final String someAnswerId = "answerId1";

        when(userRuntimeRequestComponent.getEmail()).thenReturn(voterEmail);
        doNothing().when(voteService).deleteVoteToAnswer(voterEmail, someAnswerId);

        ResponseEntity<Boolean> responseEntity = answerController.deleteVote(someAnswerId);
        assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
        assertThat(responseEntity.getBody(), is(true));

        verify(userRuntimeRequestComponent, times(1)).getEmail();
        verify(voteService, times(1)).deleteVoteToAnswer(voterEmail, someAnswerId);
    }

    @Test
    public void getAllAnswerVotes() {
        final String someAnswerId = "answerId1";

        when(voteService.findAllVotesForAnswer(someAnswerId)).thenReturn(Arrays.asList(voteAllFieldsDto));

        ResponseEntity<List<VoteAllFieldsDto>> responseEntity = answerController.getAllAnswerVotes(someAnswerId);
        List<VoteAllFieldsDto> answerVotesList = responseEntity.getBody();
        VoteAllFieldsDto voteDto = answerVotesList.get(0);

        assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
        assertThat(answerVotesList.size(), is(1));
        assertThat(voteDto.getVoteId(), is(voteAllFieldsDto.getVoteId()));
        assertThat(voteDto.isRevoked(), is(voteAllFieldsDto.isRevoked()));
        assertThat(voteDto.getAnswerBaseDto(), is(voteAllFieldsDto.getAnswerBaseDto()));
        assertThat(voteDto.getUserBaseDto(), is(voteAllFieldsDto.getUserBaseDto()));

        verify(voteService, times(1)).findAllVotesForAnswer(someAnswerId);
    }
}