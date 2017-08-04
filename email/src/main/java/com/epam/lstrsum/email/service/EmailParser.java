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

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailParser {

    public EmailForExperienceApplication getParsedMessage(@NonNull MimeMessage message) throws Exception {
        String title = message.getSubject();
        if (title == null) {
            throw new NullPointerException("There is no title");
        }
        log.debug("Subject:", title);
        if (title.trim().isEmpty()) {
            log.error("Error: received email has empty title");
            throw new EmailValidationException("Email has empty subject");
        }
        final String sender = getSender(message);
        log.debug("Sender's email address: ", sender);
        final List<String> receives = Arrays.stream(message.getAllRecipients()).map(Address::toString).collect(Collectors.toList());
        final MimeMessageParser messageParser = new MimeMessageParser(message);
        String requestText;
        String answerText = null;
        messageParser.parse();
        final String replyTo = getReplyTo(message);
        final List<DataSource> attachmentList = messageParser.getAttachmentList();
        if (replyTo == null) {
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
        return new EmailForExperienceApplication(title, requestText, answerText, sender, receives, replyTo, attachmentList);
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

        public Optional<QuestionPostDto> getQuestionPostDto() {
            final QuestionPostDto requestPostDto = new QuestionPostDto(subject, null, requestText, 0L, receivers);
            return Optional.of(requestPostDto);
        }

        public boolean hasAttachement() {
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

    private String getReplyTo(MimeMessage mimeMessage) throws MessagingException {
        if (mimeMessage.getHeader("Reply-To") == null){
            return null;
        }
        return mimeMessage.getHeader("Reply-To")[0];
    }

    private String getSender(MimeMessage mimeMessage) throws MessagingException {
        return mimeMessage.getFrom()[0].toString();
    }

    private String getAnswerTextFromHtmlAnswer(String htmlText) {
        return Jsoup.parseBodyFragment(htmlText).select("div").first().text();
    }

    private String getAnswerTextFromPlainAnswer(String plainContent) {
        final StringBuilder answerText = new StringBuilder();
        final String[] split = plainContent.split("\\n");
        for (String line : split) {
            if (line.startsWith("On") & line.endsWith("wrote:")) {
                answerText.append(line.replace(line, ""));
            } else if (line.startsWith(">")) {
                answerText.append(line.replace(line, ""));
            } else {
                answerText.append(line).append("\n");
            }
        }
        return answerText.toString();
    }

    private String getRequestTextFromHtmlAnswer(String htmlContent) {
        return Jsoup.parse(htmlContent).select("div").last().text();
    }

    private String getRequestTextFromPlainAnswer(String plainContent) {
        final StringBuilder stringBuilder = new StringBuilder();
        String[] split = plainContent.split("\\n");
        for (String line : split) {
            if (line.startsWith(">")) {
                stringBuilder.append(line.replaceFirst(">", "")).append("\n");
            }
        }
        return stringBuilder.toString();
    }
}
