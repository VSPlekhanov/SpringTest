package com.epam.lstrsum.controller;

import com.epam.lstrsum.service.AnswerService;
import com.epam.lstrsum.service.QuestionService;
import com.epam.lstrsum.service.mail.UserSynchronizer;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class AdminControllerTest {
    private QuestionService questionService = mock(QuestionService.class);
    private AnswerService answerService = mock(AnswerService.class);
    private UserSynchronizer userSynchronizer = mock(UserSynchronizer.class);

    @InjectMocks
    private AdminController adminController = new AdminController(
            questionService, answerService, userSynchronizer
    );

    @Test
    public void deleteValidQuestionWithAnswers() {
        final String validQuestionId = "1u_1r";
        doReturn(true).when(questionService).contains(validQuestionId);

        assertThat(adminController.deleteQuestionWithAnswers(validQuestionId).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(questionService, times(1)).delete(anyString());
        verify(answerService, times(1)).deleteAllAnswersOnQuestion(anyString());
    }

    @Test
    public void deleteNotValidQuestionWithAnswers() {
        final String validQuestionId = "not_valid";
        doReturn(false).when(questionService).contains(validQuestionId);

        assertThat(adminController.deleteQuestionWithAnswers(validQuestionId).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(questionService, never()).delete(anyString());
        verify(answerService, never()).deleteAllAnswersOnQuestion(anyString());
    }


    @Test
    public void forceSynchronize() {
        assertThat(adminController.forceUserSynchronization().getStatusCode())
                .isEqualTo(HttpStatus.NO_CONTENT);

        verify(userSynchronizer, times(1)).synchronizeUsers();
    }
}
