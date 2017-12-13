package com.epam.lstrsum.security;

import com.epam.lstrsum.SetUpDataBaseCollections;
import com.epam.lstrsum.controller.QuestionController;
import com.epam.lstrsum.dto.question.QuestionPostDto;
import com.epam.lstrsum.dto.user.telescope.TelescopeDataDto;
import com.epam.lstrsum.dto.user.telescope.TelescopeEmployeeEntityDto;
import com.epam.lstrsum.email.service.EmailParser;
import com.epam.lstrsum.service.MailReceiver;
import com.epam.lstrsum.service.QuestionService;
import com.epam.lstrsum.service.TelescopeService;
import com.epam.lstrsum.service.UserService;
import com.epam.lstrsum.testutils.AssertionUtils;
import org.junit.Test;
import org.mockito.Matchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.epam.lstrsum.testutils.InstantiateUtil.SOME_USER_EMAIL;
import static com.epam.lstrsum.testutils.InstantiateUtil.someQuestionPostDto;
import static com.epam.lstrsum.testutils.InstantiateUtil.someString;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ActiveProfiles("email")
public class AccessUserControlTest extends SetUpDataBaseCollections {

    @Autowired
    private QuestionController questionController;

    @Autowired
    private UserService userService;

    @Autowired
    private MailReceiver mailReceiver;

    @Autowired
    private QuestionService questionService;

    @MockBean
    private TelescopeService telescopeService;

    @MockBean
    private EmailParser emailParser;

    @Test
    public void accessForAddingQuestionByUserNotFromDB() throws IOException {
        String newCurrentUserEmail = someString();
        String newAllowedSubsEmail = someString();
        List<String> newAllowedSubsEmails = new ArrayList<>(Arrays.asList(SOME_USER_EMAIL, newAllowedSubsEmail));
        QuestionPostDto questionPostDto = someQuestionPostDto();
        questionPostDto.setAllowedSubs(newAllowedSubsEmails);

        mockTelescopeServiceByEmail(singletonList(newCurrentUserEmail));
        mockTelescopeServiceByEmail(newAllowedSubsEmails);
        when(userRuntimeRequestComponent.getEmail()).thenReturn(newCurrentUserEmail);

        int usersCount = userService.findAll().size();
        ResponseEntity<String> response = questionController.addQuestion(questionPostDto, new MultipartFile[]{});
        assertTrue(userService.findUserByEmailIfExist(newCurrentUserEmail).isPresent());
        assertTrue(userService.findUserByEmailIfExist(newAllowedSubsEmail).isPresent());
        assertThat(userService.findAll()).hasSize(usersCount + 2);
        assertThat(response).satisfies(AssertionUtils::hasStatusOk);
        assertThat(questionService.getQuestionAllFieldDtoByQuestionId(response.getBody()).getAuthor().getEmail())
                    .isEqualTo(newCurrentUserEmail.toLowerCase());
    }

    @Test
    public void accessForAddingQuestionFromEmailByUserNotFromDB() throws Exception {
        String newCurrentUserEmail = someString();
        String newAllowedSubsEmail = someString();
        List<String> newAllowedSubsEmails = Arrays.asList(SOME_USER_EMAIL, newAllowedSubsEmail);
        QuestionPostDto questionPostDto = someQuestionPostDto();
        questionPostDto.setAllowedSubs(newAllowedSubsEmails);

        mockTelescopeServiceByEmail(singletonList(newCurrentUserEmail));
        mockTelescopeServiceByEmail(newAllowedSubsEmails);
        MimeMessage mimeMessage = mockMimeMessageContentAndParsing(newCurrentUserEmail, questionPostDto);

        int usersCount = userService.findAll().size();
        mailReceiver.handleMessageWithoutBackup(mimeMessage);
        assertTrue(userService.findUserByEmailIfExist(newCurrentUserEmail).isPresent());
        assertTrue(userService.findUserByEmailIfExist(newAllowedSubsEmail).isPresent());
        assertThat(userService.findAll()).hasSize(usersCount+2);
    }

    private void mockTelescopeServiceByEmail(List<String> emails) {

        List<TelescopeEmployeeEntityDto> employeeEntityDtoList = emails
                .stream()
                .map(e -> {TelescopeDataDto currentUserTelescopeDto = TelescopeDataDto.builder()
                                                        .email(singletonList(e))
                                                        .lastName(someString())
                                                        .firstName(someString())
                                                        .build();
                        return TelescopeEmployeeEntityDto.builder().data(currentUserTelescopeDto).build();
                    })
                .collect(Collectors.toList());

        doReturn(employeeEntityDtoList).when(telescopeService).getUsersInfoByEmails(emails.stream()
                                                                                .map(String::toLowerCase)
                                                                                .collect(Collectors.toSet()));
    }

    private  MimeMessage mockMimeMessageContentAndParsing(String userEmail, QuestionPostDto questionPostDto) throws Exception {
        EmailParser.EmailForExperienceApplication parsedEmail = mock(EmailParser.EmailForExperienceApplication.class);

        MimeMessage mimeMessageMock = mock(MimeMessage.class);
        doReturn(parsedEmail).when(emailParser).getParsedMessage(Matchers.any());
        doReturn(someString()).when(mimeMessageMock).getContentType();
        doReturn(someString()).when(mimeMessageMock).getContent();
        doReturn(new InternetAddress[]{new InternetAddress(userEmail)}).when(mimeMessageMock).getFrom();
        when(parsedEmail.getSender()).thenReturn(userEmail);
        when(parsedEmail.getQuestionPostDto()).thenReturn(questionPostDto);
        when(parsedEmail.hasAttachment()).thenReturn(false);

        return mimeMessageMock;
    }
}
