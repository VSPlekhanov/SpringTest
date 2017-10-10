package com.epam.lstrsum.controller;

import com.epam.lstrsum.SetUpDataBaseCollections;
import com.epam.lstrsum.aggregators.UserAggregator;
import com.epam.lstrsum.dto.question.QuestionAppearanceDto;
import com.epam.lstrsum.dto.question.QuestionPostDto;
import com.epam.lstrsum.persistence.AttachmentRepository;
import com.epam.lstrsum.service.QuestionService;
import com.epam.lstrsum.service.TelescopeService;
import com.epam.lstrsum.service.UserService;
import com.epam.lstrsum.testutils.AssertionUtils;
import com.epam.lstrsum.testutils.InstantiateUtil;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

import static com.epam.lstrsum.testutils.InstantiateUtil.EXISTING_QUESTION_ID_WITH_ATTACHMENT;
import static com.epam.lstrsum.testutils.InstantiateUtil.EXISTING_QUESTION_ID_WITHOUT_ATTACHMENT;
import static com.epam.lstrsum.testutils.InstantiateUtil.EXISTING_ATTACHMENT_ID;
import static com.epam.lstrsum.testutils.InstantiateUtil.SOME_USER_EMAIL;
import static com.epam.lstrsum.testutils.InstantiateUtil.someMockMultipartFile;
import static com.epam.lstrsum.testutils.InstantiateUtil.someQuestionAppearanceDto;
import static com.epam.lstrsum.testutils.InstantiateUtil.someQuestionPostDto;
import static com.epam.lstrsum.testutils.InstantiateUtil.someString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
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
        addQuestionSaveAttachmentsAndReturnValidResponseTest(0);
    }

    @Test
    public void addQuestionSaveOneAttachmentsAndReturnValidResponseTest() throws Exception {
        addQuestionSaveAttachmentsAndReturnValidResponseTest(1);
    }

    @Test
    public void addQuestionSaveTwoAttachmentsAndReturnValidResponseTest() throws Exception {
        addQuestionSaveAttachmentsAndReturnValidResponseTest(2);
    }

    @Test
    public void getQuestionWithAttachmentContainsAttachmentId() throws Exception {
        String questionId = EXISTING_QUESTION_ID_WITH_ATTACHMENT;
        ResponseEntity responseEntity = questionController.getQuestionWithText(questionId);
        QuestionAppearanceDto questionAppearanceDto = (QuestionAppearanceDto) responseEntity.getBody();
        assertThat(questionAppearanceDto.getAttachments().size()).isEqualTo(1);
        assertThat(questionAppearanceDto.getAttachments().get(0).getId()).isEqualTo(EXISTING_ATTACHMENT_ID);
    }

    @Test
    public void getQuestionWithoutAttachmentNotContainsAttachmentId() throws Exception {
        String questionId = EXISTING_QUESTION_ID_WITHOUT_ATTACHMENT;
        ResponseEntity responseEntity = questionController.getQuestionWithText(questionId);
        QuestionAppearanceDto questionAppearanceDto = (QuestionAppearanceDto) responseEntity.getBody();
        assertThat(questionAppearanceDto.getAttachments()).isEmpty();
    }

    private void addQuestionSaveAttachmentsAndReturnValidResponseTest(int attachmentsCount) throws Exception {
        MockMultipartFile file = someMockMultipartFile();
        final String authorEmail = SOME_USER_EMAIL;
        final QuestionPostDto postDto = someQuestionPostDto();

        MultipartFile[] arrayMultipartFile = new MultipartFile[attachmentsCount];
        for (int i = 0; i < attachmentsCount; i++) {
            arrayMultipartFile[i] = file;
        }

        when(userRuntimeRequestComponent.getEmail()).thenReturn(authorEmail);
        Long questionCountBefore = questionService.getQuestionCount();
        Integer attachmentCountBefore = attachmentRepository.findAll().size();

        ResponseEntity responseEntity = questionController.addQuestion(postDto, arrayMultipartFile);
        assertThat(responseEntity).satisfies(AssertionUtils::hasStatusOk);
        assertThat(questionService.getQuestionCount()).isEqualTo(questionCountBefore + 1);
        assertThat(attachmentRepository.findAll().size()).isEqualTo(attachmentCountBefore + attachmentsCount);
        verify(userRuntimeRequestComponent, times(1)).getEmail();
    }
}
