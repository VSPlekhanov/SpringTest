package com.epam.lstrsum.service.impl;

import com.epam.lstrsum.dto.attachment.AttachmentAllFieldsDto;
import com.epam.lstrsum.dto.question.QuestionPostDto;
import com.epam.lstrsum.email.service.BackupHelper;
import com.epam.lstrsum.email.service.EmailParser;
import com.epam.lstrsum.enums.UserRoleType;
import com.epam.lstrsum.model.Question;
import com.epam.lstrsum.service.AttachmentService;
import com.epam.lstrsum.service.MailReceiver;
import com.epam.lstrsum.service.QuestionService;
import com.epam.lstrsum.service.TelescopeService;
import com.epam.lstrsum.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.epam.lstrsum.email.service.MailService.getAddressFrom;

@Service
@Profile("email")
@RequiredArgsConstructor
@Slf4j
public class MailReceiverImpl implements MailReceiver {
    private final UserService userService;
    private final QuestionService questionService;
    private final AttachmentService attachmentService;
    private final EmailParser emailParser;
    private final BackupHelper backupHelper;
    private final TelescopeService telescopeService;

    @Override
    @ServiceActivator(inputChannel = "receiveChannel", poller = @Poller(fixedRate = "200"))
    public void receiveMessageAndHandleIt(final MimeMessage message) throws Exception {
        Set<String> fromAddresses = getFromAddresses(message);
        if (messageNeedToHandle(fromAddresses)) {
            backupHelper.backupEmail(message);

            handleMessageWithoutBackup(message);
        } else {
            log.warn("Received email({}) from not service account", fromAddresses);
        }
    }

    @Override
    public void handleMessageWithoutBackup(final MimeMessage message) throws Exception {
        log.debug("receiveMessageAndHandleIt; Message received: {}", message);

        String contentType = message.getContentType();
        String content = "";

        String address = getAddressFrom(message.getFrom());

        log.debug("From: {}\nSubject: {}\nContentType : {}\nContent: {}", address, message.getSubject(), contentType, content);

        handleMessage(message);
    }

    private boolean messageNeedToHandle(Set<String> fromAddresses) {
        try {
            return !telescopeService.getUsersInfoByEmails(fromAddresses).isEmpty();
        } catch (RuntimeException e) {
            return false;
        }
    }

    private Set<String> getFromAddresses(MimeMessage message) {
        try {
            return Arrays.stream(message.getFrom())
                    .filter(a -> a instanceof InternetAddress)
                    .map(a -> (InternetAddress) a)
                    .map(InternetAddress::getAddress)
                    .collect(Collectors.toSet());
        } catch (MessagingException e) {
            return Collections.emptySet();
        }

    }

    private void handleMessage(final MimeMessage message) {
        log.debug("Try to handle email");
        try {
            final EmailParser.EmailForExperienceApplication parsedMessage = emailParser.getParsedMessage(message);

            final String authorEmail = parsedMessage.getSender();
            final QuestionPostDto questionPostDto = parsedMessage.getQuestionPostDto();

            final long users = userService.addIfNotExistAllWithRole(
                    questionPostDto.getAllowedSubs(), Collections.singletonList(UserRoleType.ROLE_SIMPLE_USER)
            );
            if (users > 0) {
                log.debug("Detected {} users not in base and added as another user", users);
            }

            final Question newQuestion = questionService.addNewQuestion(questionPostDto, authorEmail);

            if (parsedMessage.hasAttachment()) {
                addAllAttachments(newQuestion.getQuestionId(), parsedMessage.getAttachmentAllFieldsDto());
            }

            log.debug("Added new question with title {}", newQuestion.getTitle());
        } catch (Exception e) {
            log.warn("Can't parse received message\nWith error {}", e.getMessage());
        }
    }

    private void addAllAttachments(String questionId, List<AttachmentAllFieldsDto> attachmentAllFieldsDto) {
        List<String> attachmentIds = attachmentAllFieldsDto.stream()
                .map(this::attachExceptionally)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(AttachmentAllFieldsDto::getId)
                .collect(Collectors.toList());

        questionService.addAttachmentsToQuestion(questionId, attachmentIds);
    }

    private Optional<AttachmentAllFieldsDto> attachExceptionally(AttachmentAllFieldsDto attachmentAllFieldsDto) {
        try {
            return Optional.ofNullable(attachmentService.save(attachmentAllFieldsDto));
        } catch (Exception e) {
            log.warn("Can't add attachment to DB\nWith error {}", e.getMessage());
            return Optional.empty();
        }
    }
}
