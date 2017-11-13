package com.epam.lstrsum.controller;

import com.epam.lstrsum.dto.user.UserBaseDto;
import com.epam.lstrsum.service.AnswerService;
import com.epam.lstrsum.service.QuestionService;
import com.epam.lstrsum.service.UserService;
import com.epam.lstrsum.service.mail.UserSynchronizer;
import com.epam.lstrsum.testutils.AssertionUtils;
import org.junit.Test;
import java.util.ArrayList;
import java.util.List;

import static com.epam.lstrsum.testutils.InstantiateUtil.EXISTING_QUESTION_ID;
import static com.epam.lstrsum.testutils.InstantiateUtil.NON_EXISTING_QUESTION_ID;
import static com.epam.lstrsum.testutils.InstantiateUtil.someUserBaseDto;
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
    private UserService userService = mock(UserService.class);

    private AdminController adminController = new AdminController(
            questionService, answerService, userSynchronizer, userService
    );

    @Test
    public void getListOfUsers() throws Exception {
        UserBaseDto userBaseDto = someUserBaseDto();
        List<UserBaseDto> listUserBaseDto = new ArrayList<>();
        listUserBaseDto.add(userBaseDto);
        doReturn(listUserBaseDto).when(userService).findAllUserBaseDtos();
        List<UserBaseDto> actualList = adminController.getListOfUsers().getBody();

        assertThat(actualList).containsOnly(userBaseDto);
        verify(userService, times(1)).findAllUserBaseDtos();
    }

    @Test
    public void deleteValidQuestionWithAnswers() {
        final String validQuestionId = EXISTING_QUESTION_ID;
        doReturn(true).when(questionService).contains(validQuestionId);

        assertThat(adminController.deleteQuestionWithAnswers(validQuestionId)).satisfies(AssertionUtils::hasStatusNoContent);
        verify(questionService, times(1)).delete(anyString());
    }

    @Test
    public void deleteNotValidQuestionWithAnswers() {
        final String not_valid = NON_EXISTING_QUESTION_ID;
        doReturn(false).when(questionService).contains(not_valid);

        assertThat(adminController.deleteQuestionWithAnswers(not_valid)).satisfies(AssertionUtils::hasStatusNotFound);
        verify(questionService, never()).delete(anyString());
    }


    @Test
    public void forceSynchronize() {
        assertThat(adminController.forceUserSynchronization()).satisfies(AssertionUtils::hasStatusNoContent);

        verify(userSynchronizer, times(1)).synchronizeUsers();
    }
}
