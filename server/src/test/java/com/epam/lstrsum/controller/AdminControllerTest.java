package com.epam.lstrsum.controller;

import com.epam.lstrsum.service.AnswerService;
import com.epam.lstrsum.service.QuestionService;
import com.epam.lstrsum.service.mail.UserSynchronizer;
import com.epam.lstrsum.testutils.AssertionUtils;
import org.junit.Test;

import static com.epam.lstrsum.testutils.InstantiateUtil.EXISTING_QUESTION_ID;
import static com.epam.lstrsum.testutils.InstantiateUtil.NON_EXISTING_QUESTION_ID;
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

    private AdminController adminController = new AdminController(
            questionService, answerService, userSynchronizer
    );

    @Test
    public void deleteValidQuestionWithAnswers() {
        final String validQuestionId = EXISTING_QUESTION_ID;
        doReturn(true).when(questionService).contains(validQuestionId);

        assertThat(adminController.deleteQuestionWithAnswers(validQuestionId)).satisfies(AssertionUtils::hasStatusNoContent);
        verify(questionService, times(1)).delete(anyString());
        verify(answerService, times(1)).deleteAllAnswersOnQuestion(anyString());
    }

    @Test
    public void deleteNotValidQuestionWithAnswers() {
        final String not_valid = NON_EXISTING_QUESTION_ID;
        doReturn(false).when(questionService).contains(not_valid);

        assertThat(adminController.deleteQuestionWithAnswers(not_valid)).satisfies(AssertionUtils::hasStatusNotFound);
        verify(questionService, never()).delete(anyString());
        verify(answerService, never()).deleteAllAnswersOnQuestion(anyString());
    }


    @Test
    public void forceSynchronize() {
        assertThat(adminController.forceUserSynchronization()).satisfies(AssertionUtils::hasStatusNoContent);

        verify(userSynchronizer, times(1)).synchronizeUsers();
    }
}
