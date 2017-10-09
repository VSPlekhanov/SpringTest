package com.epam.lstrsum.controller;

import com.epam.lstrsum.SetUpDataBaseCollections;
import com.epam.lstrsum.aggregators.UserAggregator;
import com.epam.lstrsum.dto.question.QuestionPostDto;
import com.epam.lstrsum.persistence.AttachmentRepository;
import com.epam.lstrsum.service.QuestionService;
import com.epam.lstrsum.service.TelescopeService;
import com.epam.lstrsum.service.UserService;
import com.epam.lstrsum.testutils.AssertionUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import static com.epam.lstrsum.testutils.InstantiateUtil.SOME_USER_EMAIL;
import static com.epam.lstrsum.testutils.InstantiateUtil.someQuestionPostDto;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class QuestionControllerAttachmentsTest extends SetUpDataBaseCollections {
    @Autowired
    private UserService userService;

    @Autowired
    private AttachmentRepository attachmentRepository;

    @Autowired
    private QuestionController questionController;

    @Autowired
    private QuestionService questionService;

    @MockBean
    private UserRuntimeRequestComponent userRuntimeRequestComponent;

    @MockBean
    private TelescopeService telescopeService;

    @MockBean
    private UserAggregator userAggregator;

    @Test
    public void addQuestionSaveNoAttachmentsAndReturnValidResponseTest() throws Exception {
        MockMultipartFile file = new MockMultipartFile("name", "originalName", "contentType", new byte[5]);

        final String authorEmail = SOME_USER_EMAIL;
        final QuestionPostDto postDto = someQuestionPostDto();
        MultipartFile[] arrayMultipartFile = {}; // question with 0 attachments

        when(userRuntimeRequestComponent.getEmail()).thenReturn(authorEmail);

        Long questionCount = questionService.getQuestionCount();
        Integer attachmentCount = attachmentRepository.findAll().size();

        ResponseEntity responseEntity = questionController.addQuestion(postDto, arrayMultipartFile);
        assertThat(responseEntity).satisfies(AssertionUtils::hasStatusOk);
        assertThat(questionService.getQuestionCount()).isEqualTo(questionCount + 1);
        assertThat(attachmentRepository.findAll().size()).isEqualTo(attachmentCount);
        verify(userRuntimeRequestComponent, times(1)).getEmail();
    }

    @Test
    public void addQuestionSaveOneAttachmentsAndReturnValidResponseTest() throws Exception {
        MockMultipartFile file = new MockMultipartFile("name", "originalName", "contentType", new byte[5]);

        final String authorEmail = SOME_USER_EMAIL;
        final QuestionPostDto postDto = someQuestionPostDto();
        MultipartFile[] arrayMultipartFile = {file}; // question with 1 attachment

        when(userRuntimeRequestComponent.getEmail()).thenReturn(authorEmail);

        Long questionCount = questionService.getQuestionCount();
        Integer attachmentCount = attachmentRepository.findAll().size();

        ResponseEntity responseEntity = questionController.addQuestion(postDto, arrayMultipartFile);
        assertThat(responseEntity).satisfies(AssertionUtils::hasStatusOk);
        assertThat(questionService.getQuestionCount()).isEqualTo(questionCount + 1);
        assertThat(attachmentRepository.findAll().size()).isEqualTo(attachmentCount + 1);
        verify(userRuntimeRequestComponent, times(1)).getEmail();
    }

    @Test
    public void addQuestionSaveTwoAttachmentsAndReturnValidResponseTest() throws Exception {
        MockMultipartFile file = new MockMultipartFile("name", "originalName", "contentType", new byte[5]);

        final String authorEmail = SOME_USER_EMAIL;
        final QuestionPostDto postDto = someQuestionPostDto();
        MultipartFile[] arrayMultipartFile = {file,file}; // question with 2 attachments

        when(userRuntimeRequestComponent.getEmail()).thenReturn(authorEmail);

        Long questionCount = questionService.getQuestionCount();
        Integer attachmentCount = attachmentRepository.findAll().size();

        ResponseEntity responseEntity = questionController.addQuestion(postDto, arrayMultipartFile);
        assertThat(responseEntity).satisfies(AssertionUtils::hasStatusOk);
        assertThat(questionService.getQuestionCount()).isEqualTo(questionCount + 1);
        assertThat(attachmentRepository.findAll().size()).isEqualTo(attachmentCount + 2);
        verify(userRuntimeRequestComponent, times(1)).getEmail();
    }
}
