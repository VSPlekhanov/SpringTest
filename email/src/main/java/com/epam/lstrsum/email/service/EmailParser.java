package com.epam.lstrsum.email.service;


import com.epam.lstrsum.dto.attachment.AttachmentAllFieldsDto;
import com.epam.lstrsum.dto.question.QuestionPostDto;
import com.epam.lstrsum.email.exception.EmailValidationException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.mail.util.MimeMessageParser;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import javax.activation.DataSource;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.epam.lstrsum.email.service.EmailParserUtil.evalInlineSources;
import static com.epam.lstrsum.email.service.EmailParserUtil.evalKeysForInlining;
import static com.epam.lstrsum.email.service.EmailParserUtil.getReplyTo;
import static com.epam.lstrsum.email.service.EmailParserUtil.getSender;
import static com.epam.lstrsum.email.service.EmailParserUtil.replaceStringKeys;
import static com.epam.lstrsum.email.service.EmailParserUtil.stringIsEmpty;
import static java.util.Objects.isNull;

@Profile("email")
@Service
@Slf4j
@RequiredArgsConstructor
public class EmailParser {
    private final ExchangeServiceHelper exchangeServiceHelper;

    @Value("#{'${multipart.allowed-extensions}'.split(',')}")
    private List<String> allowedExtensions;

    @Value("${email.distribution-list}")
    private String distributionList;

    @Value("${spring.mail.username}")
    private String fromAddress;

    public EmailForExperienceApplication getParsedMessage(@NonNull MimeMessage message) throws Exception {

        String title = message.getSubject();
        validateNotEmptyString(title);

        final String sender = getSender(message);
        log.debug("Sender's email address: {}", sender);

        final MimeMessageParser messageParser = new MimeMessageParser(message);
        messageParser.parse();

        final String replyTo = getReplyTo(message);
        String questionText;

        List<String> keys = new ArrayList<>();
        List<DataSource> inlineSources = new ArrayList<>();

        if (isQuestion(replyTo)) {
            log.debug("Received email is question");
            if (messageParser.hasPlainContent()) {
                questionText = messageParser.getPlainContent();
                validateQuestion(questionText);

            } else if (messageParser.hasHtmlContent()) {
                questionText = messageParser.getHtmlContent();
                validateQuestion(questionText);

                keys.addAll(evalKeysForInlining(questionText));
                inlineSources.addAll(evalInlineSources(messageParser, questionText));
            } else {
                log.error("Error: has wrong format");
                throw new EmailValidationException("Email has wrong format");
            }
        } else {
            log.debug("Received email is answer. We do not handle it");
            throw new UnsupportedOperationException("Answer is not our business");
        }

        questionText = replaceStringKeys(questionText, keys);

        List<DataSource> attachmentListWithoutInline = messageParser.getAttachmentList();
        attachmentListWithoutInline.removeAll(inlineSources);

        return new EmailForExperienceApplication(
                title, questionText, sender,
                getReceiversFromMessage(message), attachmentListWithoutInline,
                inlineSources
        );

    }

    private List<String> getReceiversFromMessage(MimeMessage message) throws MessagingException {
        return Arrays.stream(message.getAllRecipients())
                .map(i -> (InternetAddress) i)
                .map(InternetAddress::getAddress)
                .filter(e -> !e.equalsIgnoreCase(distributionList))
                .flatMap(email -> exchangeServiceHelper.resolveGroup(email).stream())
                .filter(e -> !e.equalsIgnoreCase(fromAddress))
                .map(String::toLowerCase)
                .collect(Collectors.toList());
    }

    private boolean isQuestion(String replyTo) {
        return isNull(replyTo);
    }

    private void validateNotEmptyString(String title) {
        if (isNull(title)) {
            throw new NullPointerException("There is no title");
        }
        log.debug("Subject: {}", title);
        if (title.trim().isEmpty()) {
            log.error("Error: received email has empty title");
            throw new EmailValidationException("Email has empty subject");
        }
    }

    private void validateQuestion(String question) {
        if (stringIsEmpty(question)) {
            log.error("Error: Email has empty body");
            throw new EmailValidationException("Email has empty body");
        }
    }

    private boolean notAllowedFile(String fileName) {
        return allowedExtensions.stream()
                .noneMatch(fileName::endsWith);
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public class EmailForExperienceApplication {

        @NonNull
        private final String subject;
        @NonNull
        private final String questionText;

        @Getter
        @NonNull
        private final String sender;

        @Getter
        @NonNull
        private final List<String> receivers;

        @NonNull
        private final List<DataSource> attacheDataSourceList;

        @NonNull
        private final List<DataSource> inlineSources;

        public QuestionPostDto getQuestionPostDto() {
            List<byte[]> toByteArray = inlineSources.stream()
                    .map(this::convertExceptionally)
                    .collect(Collectors.toList());

            return new QuestionPostDto(subject, null, questionText,
                    0L, receivers, toByteArray
            );
        }

        private byte[] convertExceptionally(DataSource dataSource) {
            try {
                return IOUtils.toByteArray(dataSource.getInputStream());
            } catch (Exception e) {
                log.error("Can't convert DataSource to byte array\nWith error {}", e.getMessage());
                return new byte[0];
            }
        }

        public boolean hasAttachment() {
            return !attacheDataSourceList.isEmpty();
        }

        public List<AttachmentAllFieldsDto> getAttachmentAllFieldsDto() throws IOException {
            final List<AttachmentAllFieldsDto> attached = new ArrayList<>();
            for (DataSource datasource : attacheDataSourceList) {
                final String fileName = datasource.getName();

                log.debug("Attachment file name: {}", fileName);

                if (notAllowedFile(fileName)) {
                    continue;
                }

                final String fileType = datasource.getContentType();
                final byte[] data = IOUtils.toByteArray(datasource.getInputStream());
                attached.add(new AttachmentAllFieldsDto(new ObjectId().toString(), fileName, fileType, data));
            }
            return attached;
        }
    }
}
