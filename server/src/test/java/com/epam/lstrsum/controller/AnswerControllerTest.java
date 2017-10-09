package com.epam.lstrsum.controller;

import com.epam.lstrsum.dto.answer.AnswerPostDto;
import com.epam.lstrsum.dto.common.CounterDto;
import com.epam.lstrsum.service.AnswerService;
import com.epam.lstrsum.service.VoteService;
import com.epam.lstrsum.testutils.AssertionUtils;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.http.ResponseEntity;

import static com.epam.lstrsum.testutils.InstantiateUtil.EXISTING_QUESTION_ID;
import static com.epam.lstrsum.testutils.InstantiateUtil.someAnswerPostDto;
import static com.epam.lstrsum.testutils.InstantiateUtil.someInt;
import static com.epam.lstrsum.testutils.InstantiateUtil.someLong;
import static com.epam.lstrsum.testutils.InstantiateUtil.someString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class AnswerControllerTest {
    @Mock
    private AnswerService answerService = mock(AnswerService.class);

    @Mock
    private VoteService voteService = mock(VoteService.class);

    @Mock
    private UserRuntimeRequestComponent userRuntimeRequestComponent = mock(UserRuntimeRequestComponent.class);

    private AnswerController controller = new AnswerController(
            answerService, userRuntimeRequestComponent, voteService
    );

    @Test
    public void addNewAnswerTest() throws Exception {
        final AnswerPostDto answer = someAnswerPostDto();
        final String email = someString();
        when(userRuntimeRequestComponent.getEmail()).thenReturn(email);

        assertThat(controller.addAnswer(answer)).satisfies(AssertionUtils::hasStatusOk);
        verify(answerService, times(1)).addNewAnswer(eq(answer), eq(email));
    }

    @Test
    public void voteForAnswerExists() {
        doReturn(true).when(voteService)
                .voteForAnswerByUser(anyString(), anyString());

        String someAnswerId = someString();
        assertThat(controller.voteFor(someAnswerId))
                .satisfies(AssertionUtils::hasStatusNoContent);
    }

    @Test
    public void voteForAnswerDoNotExists() {
        doReturn(false).when(voteService)
                .voteForAnswerByUser(anyString(), anyString());

        String someAnswerId = someString();
        assertThat(controller.voteFor(someAnswerId))
                .satisfies(AssertionUtils::hasStatusNotFound);
    }

    @Test
    public void unvoteForAnswerExists() {
        doReturn(true).when(voteService)
                .unvoteForAnswerByUser(anyString(), anyString());

        String someAnswerId = someString();
        assertThat(controller.unvoteFor(someAnswerId))
                .satisfies(AssertionUtils::hasStatusNoContent);
    }

    @Test
    public void unvoteForAnswerDoNotExists() {
        doReturn(false).when(voteService)
                .unvoteForAnswerByUser(anyString(), anyString());

        String someAnswerId = someString();
        assertThat(controller.unvoteFor(someAnswerId))
                .satisfies(AssertionUtils::hasStatusNotFound);
    }

    @Test
    public void getAnswersByQuestionId() {
        assertThat(controller.getAnswersByQuestionId(EXISTING_QUESTION_ID, someInt(), someInt()))
                .satisfies(AssertionUtils::hasStatusOk);

        verify(answerService, times(1)).getAnswersByQuestionId(anyString(), anyInt(), anyInt());
    }

    @Test
    public void getAnswerCountByQuestionId() {
        assertThat(controller.getAnswerCountByQuestionId(EXISTING_QUESTION_ID)).satisfies(AssertionUtils::hasStatusOk);
        verify(answerService, times(1)).getAnswerCountByQuestionId(anyString());
    }
}