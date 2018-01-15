package com.epam.lstrsum.email.service;


import com.epam.lstrsum.dto.attachment.AttachmentAllFieldsDto;
import com.epam.lstrsum.dto.question.QuestionPostDto;
import com.epam.lstrsum.email.exception.EmailValidationException;
import com.epam.lstrsum.email.template.MailTemplate;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.mail.util.MimeMessageParser;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.Objects;
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

    @Autowired
    @Setter
    private MailService mailService;

    @Autowired
    @Setter
    private MailTemplate<List> newErrorNotificationTemplate;

    @Value("${email.max-attachments-number}")
    @Setter
    private int maxAttachmentsNumber;

    @Value("${email.max-text-size}")
    @Setter
    private int maxTextSize;

    @Value("${email.max-attachment-size}")
    @Setter
    private int maxAttachmentSize;

    public EmailForExperienceApplication getParsedMessage(@NonNull MimeMessage message) throws Exception {
        List<String> emailParsingInfo = new ArrayList<>();

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

        boolean textExceededSize = false;

        if (isQuestion(replyTo)) {
            log.debug("Received email is question");
            if (messageParser.hasPlainContent()) {
                questionText = messageParser.getPlainContent();

                String errorMessage = validateTextSize(questionText.length());

                textExceededSize = Objects.nonNull(errorMessage);

                if (Objects.nonNull(errorMessage)) {
                    emailParsingInfo.add(errorMessage);
                }


                validateQuestion(questionText);

            } else if (messageParser.hasHtmlContent()) {
                questionText = messageParser.getHtmlContent();
                validateQuestion(questionText);

                String errorMessage = validateTextSize(questionText.length());

                textExceededSize = Objects.nonNull(errorMessage);

                if (Objects.nonNull(errorMessage)) {
                    emailParsingInfo.add(errorMessage);
                }

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

        List<DataSource> parsedAttachmentsWithoutInline = parseAttachmentsWithoutInline(attachmentListWithoutInline);

        String errorMessage = validateSizeOfEachParsedAttachment(parsedAttachmentsWithoutInline.size(), attachmentListWithoutInline.size());

        if (Objects.nonNull(errorMessage)) {
            emailParsingInfo.add(errorMessage);
        }

        errorMessage = validateSizeOfParsedAttachmentsList(parsedAttachmentsWithoutInline.size());

        if (Objects.nonNull(errorMessage)) {
            parsedAttachmentsWithoutInline = parsedAttachmentsWithoutInline.subList(0,10);
            emailParsingInfo.add(errorMessage);
        }

        emailParsingInfo.add("The question was" + (textExceededSize ? "n't" : "") + " created!");
        emailParsingInfo.add(sender);

        if (emailParsingInfo.size() > 2) {
            mailService.sendMessages(newErrorNotificationTemplate.buildMailMessages(emailParsingInfo, false));
        }

        if (textExceededSize) {
            throw new EmailValidationException(emailParsingInfo.get(0));
        }

        return new EmailForExperienceApplication(
                title, questionText, sender,
                getReceiversFromMessage(message), parsedAttachmentsWithoutInline,
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

    private long bytesToMegabytes(long bytes) {
        return bytes / 1024 / 1024;
    }

    private boolean validateAttachmentSize(long bytes) {
        return bytesToMegabytes(bytes) < maxAttachmentSize;
    }

    private String validateTextSize(long textLength) {
        if(bytesToMegabytes(Character.BYTES * textLength) >= maxTextSize) {
            String errorMessage = "Text size exceeded limit - " + maxTextSize + "MB, question wasn't created";
            log.error(errorMessage);
            return errorMessage;
        }
        return null;
    }

    private List<DataSource> parseAttachmentsWithoutInline(List<DataSource> attachmentListWithoutInline) throws Exception {
        List<DataSource> parsedAttachments = attachmentListWithoutInline;
        for (DataSource attachment: attachmentListWithoutInline) {
            if (!validateAttachmentSize(attachment.getInputStream().available())) {

                parsedAttachments = parsedAttachments.stream()
                        .filter(o -> {
                            try {
                                return validateAttachmentSize(o.getInputStream().available());
                            } catch (IOException e) {
                                log.error("Error during attachments size checking");
                                return false;
                            }
                        })
                        .collect(Collectors.toList());

                break;
            }
        }
        return parsedAttachments;
    }

    private String validateSizeOfEachParsedAttachment(int parsedListSize, int originalListSize) {
        if (parsedListSize != originalListSize) {
            String errorMessage = "Some attachments exceeded size limit - " + maxAttachmentSize + "MB";
            log.warn(errorMessage);
            return errorMessage;
        }
        return null;
    }

    private String validateSizeOfParsedAttachmentsList(int parsedListSize) {
        if (parsedListSize >= maxAttachmentsNumber) {
            String errorMessage = "Number of attachments exceeded limit - " + maxAttachmentsNumber + ", extra attachments were ignored";
            log.warn(errorMessage);
            return errorMessage;
        }
        return null;
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
