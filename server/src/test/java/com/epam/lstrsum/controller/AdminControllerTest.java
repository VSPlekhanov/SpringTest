package com.epam.lstrsum.controller;

import com.epam.lstrsum.SetUpDataBaseCollections;
import com.epam.lstrsum.dto.user.UserBaseDto;
import com.epam.lstrsum.model.User;
import com.epam.lstrsum.service.AnswerService;
import com.epam.lstrsum.service.QuestionService;
import com.epam.lstrsum.service.UserService;
import com.epam.lstrsum.service.mail.UserSynchronizer;
import com.epam.lstrsum.testutils.AssertionUtils;
import com.google.common.collect.ImmutableList;
import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static com.epam.lstrsum.testutils.InstantiateUtil.EXISTING_QUESTION_ID;
import static com.epam.lstrsum.testutils.InstantiateUtil.NON_EXISTING_QUESTION_ID;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class AdminControllerTest extends SetUpDataBaseCollections {
    @Autowired
    private TestRestTemplate restTemplate;

    private QuestionService questionService = mock(QuestionService.class);
    private AnswerService answerService = mock(AnswerService.class);
    private UserSynchronizer userSynchronizer = mock(UserSynchronizer.class);
    private UserService userService = mock(UserService.class);

    private AdminController adminController = new AdminController(
            questionService, answerService, userSynchronizer, userService
    );

    @Test
    public void getListOfUsers() throws Exception {
        ResponseEntity<List<UserBaseDto>> responseEntity = restTemplate.exchange("/admin/user/list",
                HttpMethod.GET, null, new ParameterizedTypeReference<List<UserBaseDto>>() {
                });
        List<UserBaseDto> actualList = responseEntity.getBody();
        //validate
        MatcherAssert.assertThat(actualList.size(), is(7));
        List<String> actualIds = actualList.stream().map(UserBaseDto::getUserId).collect(collectingAndThen(toList(), ImmutableList::copyOf));
        MatcherAssert.assertThat(actualIds, containsInAnyOrder("1u", "2u", "3u", "4u", "5u", "6u", "7u"));
    }

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
