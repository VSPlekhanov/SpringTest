package com.epam.lstrsum.service.mail;

import com.epam.lstrsum.aggregators.QuestionAggregator;
import com.epam.lstrsum.dto.question.QuestionPostDto;
import com.epam.lstrsum.email.service.EmailParser;
import com.epam.lstrsum.email.service.ExchangeServiceHelper;
import com.epam.lstrsum.email.service.MailService;
import com.epam.lstrsum.model.Question;
import com.epam.lstrsum.service.QuestionService;
import com.epam.lstrsum.service.SubscriptionService;
import com.epam.lstrsum.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.stereotype.Service;

import javax.mail.internet.MimeMessage;

import static com.epam.lstrsum.email.service.MailService.getAddressFrom;

@Service
@Profile("email")
@RequiredArgsConstructor
@Slf4j
public class MailReceiver {
    private static final String INTERNET_ADDRESS_TYPE = "rfc822";

    private final MailService mailService;
    private final UserService userService;
    private final QuestionService questionService;
    private final EmailParser emailParser;

    @ServiceActivator(inputChannel = "receiveChannel", poller = @Poller(fixedRate = "200"))
    public void showMessages(final MimeMessage message) throws Exception {
        log.debug("showMessages; Message received: {}", message);

        mailService.backupEmail(message);

        String contentType = message.getContentType();
        String content = "";

        String address = getAddressFrom(message.getFrom());

        log.debug("From: {}\nSubject: {}\nContentType : {}\nContent: {}", address, message.getSubject(), contentType, content);

        handleMessage(message);
    }

    private void handleMessage(final MimeMessage message) {
        log.debug("Try to handle email");
        try {
            final EmailParser.EmailForExperienceApplication parsedMessage = emailParser.getParsedMessage(message);

            final String authorEmail = parsedMessage.getSender();
            final QuestionPostDto questionPostDto = parsedMessage.getQuestionPostDto();

            final long users = userService.addIfNotExistAllWithRole(
                    questionPostDto.getAllowedSubs(), new String[]{"ANOTHER_USER"}
            );
            if (users > 0) {
                log.debug("Detected {} users not in base and added as another user", users);
            }

            final Question newQuestion = questionService.addNewQuestion(questionPostDto, authorEmail);
            log.debug("Added new question with title {}", newQuestion.getTitle());
        } catch (Exception e) {
            log.warn("Can't parse received message\nWith error {}", e.getMessage());
        }
    }
}
