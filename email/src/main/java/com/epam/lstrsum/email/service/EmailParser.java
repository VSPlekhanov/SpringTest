package com.epam.lstrsum.email.service;


import com.epam.lstrsum.dto.answer.AnswerPostDto;
import com.epam.lstrsum.dto.attachment.AttachmentAllFieldsDto;
import com.epam.lstrsum.dto.question.QuestionPostDto;
import com.epam.lstrsum.email.exception.EmailValidationException;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.mail.util.MimeMessageParser;
import org.bson.types.ObjectId;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;

import javax.activation.DataSource;
import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.epam.lstrsum.email.service.EmailParserUtil.*;
import static java.util.Objects.isNull;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailParser {

    public EmailForExperienceApplication getParsedMessage(@NonNull MimeMessage message) throws Exception {
        String title = message.getSubject();
        validateNotEmptyString(title);

        final String sender = getSender(message);
        log.debug("Sender's email address: {}", sender);

        final MimeMessageParser messageParser = new MimeMessageParser(message);
        messageParser.parse();

        String requestText;
        String answerText = null;
        final String replyTo = getReplyTo(message);

        if (mailIsAnswer(replyTo)) {
            log.debug("Received email is answer");
            if (messageParser.hasPlainContent()) {
                log.debug("email's type is plain/text");
                requestText = messageParser.getPlainContent();
                if (requestText.trim().isEmpty()) {
                    log.error("Error: Email has empty body");
                    throw new EmailValidationException("Email has empty body");
                }
            } else if (messageParser.hasHtmlContent()) {
                log.debug("email's type is plain/html");
                requestText = Jsoup.parseBodyFragment(messageParser.getHtmlContent()).select("body").text();
                if (requestText.trim().isEmpty()) {
                    log.error("Error: Email has empty body");
                    throw new EmailValidationException("Email has empty body");
                }
            } else {
                log.error("Error: has wrong format");
                throw new EmailValidationException("Email has wrong format");
            }
        } else {
            log.debug("Received email is request");
            title = title.replaceFirst("Re:", "");
            if (messageParser.hasPlainContent()) {
                log.debug("email's type is plain/text");
                requestText = getRequestTextFromPlainAnswer(messageParser.getPlainContent());
                answerText = getAnswerTextFromPlainAnswer(messageParser.getPlainContent());
            } else if (messageParser.hasHtmlContent()) {
                log.debug("email's type is plain/html");
                requestText = getRequestTextFromHtmlAnswer(messageParser.getHtmlContent());
                answerText = getAnswerTextFromHtmlAnswer(messageParser.getHtmlContent());
            } else {
                log.error("Error: Email has empty body");
                throw new EmailValidationException("Email has empty body");
            }
        }

        return new EmailForExperienceApplication(
                title, requestText, answerText,
                sender, getReceiversFromMessage(message), replyTo,
                messageParser.getAttachmentList()
        );
    }

    private List<String> getReceiversFromMessage(MimeMessage message) throws MessagingException {
        return Arrays.stream(message.getAllRecipients())
                .map(Address::toString)
                .collect(Collectors.toList());
    }

    private boolean mailIsAnswer(String replyTo) {
        return isNull(replyTo);
    }

    private void validateNotEmptyString(String title) {
        if (isNull(title)) {
            throw new NullPointerException("There is no title");
        }
        log.debug("Subject:", title);
        if (title.trim().isEmpty()) {
            log.error("Error: received email has empty title");
            throw new EmailValidationException("Email has empty subject");
        }
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public class EmailForExperienceApplication {

        @NonNull
        private final String subject;
        @NonNull
        private final String requestText;
        private final String answerText;

        @Getter
        @NonNull
        private final String sender;
        @NonNull
        @Getter
        private final List<String> receivers;
        private final String replier;
        @NonNull
        private final List<DataSource> attacheDataSourceList;

        public boolean isAnswer() {
            return replier != null & answerText != null;
        }

        // TODO: Should be implemented after AnswerPostDto refactoring
        public Optional<AnswerPostDto> getAnswerPostDto() {
            throw new UnsupportedOperationException("Not implemented yet");
        }

        public QuestionPostDto getQuestionPostDto() {
            return new QuestionPostDto(subject, null, requestText, 0L, receivers);
        }

        public boolean hasAttachment() {
            return !attacheDataSourceList.isEmpty();
        }

        public List<AttachmentAllFieldsDto> getAttachmentAllFieldsDto() throws IOException {
            final List<AttachmentAllFieldsDto> attached = new ArrayList<>();
            for (DataSource datasource : attacheDataSourceList) {
                final String fileName = datasource.getName();
                final String fileType = datasource.getContentType();
                final byte[] data = IOUtils.toByteArray(datasource.getInputStream());
                attached.add(new AttachmentAllFieldsDto(new ObjectId().toString(), fileName, fileType, data));
            }
            return attached;
        }
    }

    private String getAnswerTextFromPlainAnswer(String plainContent) {
        return Arrays.stream(plainContent.split("\\n"))
                .map(EmailParserUtil::mapPlainAnswerString)
                .collect(Collectors.joining());
    }

    private String getRequestTextFromPlainAnswer(String plainContent) {
        return Arrays.stream(plainContent.split("\\n"))
                .filter(line -> line.startsWith(">"))
                .map(line -> line.replaceFirst(">", ""))
                .collect(Collectors.joining());
    }
}
