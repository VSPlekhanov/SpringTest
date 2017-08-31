package com.epam.lstrsum.controller;

import com.epam.lstrsum.aggregators.AnswerAggregator;
import com.epam.lstrsum.dto.answer.AnswerAllFieldsDto;
import com.epam.lstrsum.dto.answer.AnswerPostDto;
import com.epam.lstrsum.dto.question.QuestionBaseDto;
import com.epam.lstrsum.dto.user.UserBaseDto;
import com.epam.lstrsum.enums.UserRoleType;
import com.epam.lstrsum.exception.AnswerValidationException;
import com.epam.lstrsum.model.Answer;
import com.epam.lstrsum.model.Question;
import com.epam.lstrsum.model.User;
import com.epam.lstrsum.service.AnswerService;
import com.epam.lstrsum.service.VoteService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.time.Instant;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class AnswerControllerTest {
    @Mock
    private AnswerService answerService = mock(AnswerService.class);

    @Mock
    private AnswerAggregator answerAggregator = mock(AnswerAggregator.class);

    @Mock
    private VoteService voteService = mock(VoteService.class);

    @Mock
    private UserRuntimeRequestComponent userRuntimeRequestComponent = mock(UserRuntimeRequestComponent.class);

    private AnswerController answerController = new AnswerController(
            answerService, userRuntimeRequestComponent, voteService
    );

    private String questionId = "1u_3r";
    private AnswerPostDto answerPostDto;
    private AnswerAllFieldsDto answerAllFieldsDto;
    private Answer answer;
    private ResponseEntity<AnswerAllFieldsDto> responseEntity;
    private String authorEmail = "John_Doe@epam.com";

    private static <T> void hasStatusOk(ResponseEntity<T> responseEntity) {
        assertEntityWithStatus(responseEntity, HttpStatus.OK);
    }

    private static <T> void hasStatusNotFound(ResponseEntity<T> responseEntity) {
        assertEntityWithStatus(responseEntity, HttpStatus.NOT_FOUND);
    }

    @Test(expected = AnswerValidationException.class)
    //TODO Test purposes only
    public void addNewAnswerNoQuestionTest() throws IOException {
        answerPostDto = new AnswerPostDto(null, "text");
        when(userRuntimeRequestComponent.getEmail()).thenReturn("John_Doe@epam.com");
        doThrow(AnswerValidationException.class).when(answerService).addNewAnswer(eq(answerPostDto), eq(authorEmail));
        responseEntity = answerController.addAnswer(answerPostDto);
    }

    @Test(expected = AnswerValidationException.class)
    //TODO Test purposes only
    public void addNewAnswerWithNoTextTest() throws IOException {
        answerPostDto = new AnswerPostDto(questionId, null);
        when(userRuntimeRequestComponent.getEmail()).thenReturn("John_Doe@epam.com");
        doThrow(AnswerValidationException.class).when(answerService).addNewAnswer(eq(answerPostDto), eq(authorEmail));
        responseEntity = answerController.addAnswer(answerPostDto);
    }

    private static <T> void hasStatusNoContent(ResponseEntity<T> responseEntity) {
        assertEntityWithStatus(responseEntity, HttpStatus.NO_CONTENT);
    }

    private static <T> void assertEntityWithStatus(ResponseEntity<T> responseEntity, HttpStatus httpStatus) {
        assertThat(responseEntity.getStatusCode())
                .isEqualTo(httpStatus);
    }

    @Before
    public void setUp() {
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
                                Collections.singletonList(UserRoleType.EXTENDED_USER),
                                Instant.now(),
                                true))
                        .allowedSubs(Collections.emptyList())
                        .upVote(0)
                        .build(),
                "text",
                Instant.now(),
                new User(
                        "userId1",
                        "John",
                        "Doe",
                        authorEmail,
                        Collections.singletonList(UserRoleType.EXTENDED_USER),
                        Instant.now(),
                        true),
                Collections.emptyList());
    }

    @Test
    public void addNewAnswerTest() throws IOException {
        answerPostDto = new AnswerPostDto(questionId, "text");
        when(answerService.addNewAnswer(answerPostDto, authorEmail)).thenReturn(answerAllFieldsDto);
        when(userRuntimeRequestComponent.getEmail()).thenReturn("John_Doe@epam.com");
        when(answerAggregator.modelToAllFieldsDto(answer)).thenReturn(answerAllFieldsDto);
        responseEntity = answerController.addAnswer(answerPostDto);
        verify(answerService).addNewAnswer(answerPostDto, authorEmail);

        assertThat(responseEntity.getBody())
                .satisfies(
                        entity -> {
                            assertThat(entity.getAuthorId()).isNotNull();
                            assertThat(entity.getQuestionId()).isNotNull();
                            assertThat(entity.getCreatedAt()).isNotNull();
                            assertThat(entity.getText()).isEqualTo("text");
                            assertThat(entity.getUpVote()).isEqualTo(0);
                        }
                );
        assertThat(responseEntity)
                .satisfies(AnswerControllerTest::hasStatusOk);
    }

    @Test
    public void voteForAnswerExists() {
        doReturn(true).when(voteService)
                .voteForAnswerByUser(anyString(), anyString());

        String someAnswerId = "someAnswerId";
        assertThat(answerController.voteFor(someAnswerId))
                .satisfies(AnswerControllerTest::hasStatusNoContent);
    }

    @Test
    public void voteForAnswerDoNotExists() {
        doReturn(false).when(voteService)
                .voteForAnswerByUser(anyString(), anyString());

        String someAnswerId = "someAnswerId";
        assertThat(answerController.voteFor(someAnswerId))
                .satisfies(AnswerControllerTest::hasStatusNotFound);
    }

    @Test
    public void unvoteForAnswerExists() {
        doReturn(true).when(voteService)
                .unvoteForAnswerByUser(anyString(), anyString());

        String someAnswerId = "someAnswerId";
        assertThat(answerController.unvoteFor(someAnswerId))
                .satisfies(AnswerControllerTest::hasStatusNoContent);
    }

    @Test
    public void unvoteForAnswerDoNotExists() {
        doReturn(false).when(voteService)
                .unvoteForAnswerByUser(anyString(), anyString());

        String someAnswerId = "someAnswerId";
        assertThat(answerController.unvoteFor(someAnswerId))
                .satisfies(AnswerControllerTest::hasStatusNotFound);
    }
}