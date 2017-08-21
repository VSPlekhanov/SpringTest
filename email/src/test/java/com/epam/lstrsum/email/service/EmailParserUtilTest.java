package com.epam.lstrsum.email.service;

import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.apache.commons.mail.util.MimeMessageParser;
import org.junit.Test;

import javax.mail.Address;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.util.ByteArrayDataSource;
import java.io.File;
import java.io.IOException;
import java.util.Collections;

import static com.epam.lstrsum.email.service.EmailParserUtil.stringIsEmpty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EmailParserUtilTest {
    private MimeMessage mimeMessage = mock(MimeMessage.class);

    @Test
    public void getReplyToReturnNullFromNull() throws Exception {
        when(mimeMessage.getHeader("Reply-To")).thenReturn(null);

        assertThat(EmailParserUtil.getReplyTo(mimeMessage))
                .isNull();

    }

    @Test
    public void stringIsEmptyTrue() {
        assertThat(stringIsEmpty("")).isTrue();
        assertThat(stringIsEmpty("           ")).isTrue();
    }

    @Test
    public void stringIsEmptyFalse() {
        assertThat(stringIsEmpty("a")).isFalse();
        assertThat(stringIsEmpty("          as ")).isFalse();
    }


    @Test
    public void getReplyToReturnNullFromEmptyArray() throws Exception {
        when(mimeMessage.getHeader("Reply-To")).thenReturn(new String[]{});

        assertThat(EmailParserUtil.getReplyTo(mimeMessage))
                .isNull();

    }

    @Test
    public void getReplyToReturnCorrect() throws Exception {
        when(mimeMessage.getHeader("Reply-To")).thenReturn(new String[]{"first", "second"});

        assertThat(EmailParserUtil.getReplyTo(mimeMessage))
                .isEqualTo("first");
    }

    @Test
    public void getSender() throws Exception {
        when(mimeMessage.getFrom()).thenReturn(new Address[]{new InternetAddress("first")});

        assertThat(EmailParserUtil.getSender(mimeMessage))
                .isEqualTo("first");
    }

    @Test
    public void evalKeysForInliningFromHtmlFile() throws IOException {
        String htmlContent = FileUtils.readFileToString(new File("src/test/resources/mail/message-example.html"), "UTF-8");

        assertThat(EmailParserUtil.evalKeysForInlining(htmlContent))
                .containsOnlyOnce("cid:part1.6EA1AD67.DB23CF30@epam.com");
    }

    @Test
    public void evalKeysForInliningFromEmptyString() {
        String htmlContent = "";

        assertThat(EmailParserUtil.evalKeysForInlining(htmlContent))
                .hasSize(0);
    }

    @Test
    public void evalInlineSources() throws Exception {
        String htmlContent = FileUtils.readFileToString(new File("src/test/resources/mail/message-example.html"), "UTF-8");
        MimeMessageParser mimeMessageParser = mock(MimeMessageParser.class);
        String key = "part1.6EA1AD67.DB23CF30@epam.com";

        ByteArrayDataSource expected = new ByteArrayDataSource("", "");
        doReturn(expected)
                .when(mimeMessageParser).findAttachmentByCid(key);

        assertThat(EmailParserUtil.evalInlineSources(mimeMessageParser, htmlContent))
                .containsOnly(expected);

        verify(mimeMessageParser, times(1))
                .findAttachmentByCid(key);
    }

    @Test
    @SneakyThrows
    public void replaceStringKeys() {
        String htmlContent = FileUtils.readFileToString(new File("src/test/resources/mail/message-example.html"), "UTF-8");

        String key = "cid:part1.6EA1AD67.DB23CF30@epam.com";
        assertThat(EmailParserUtil.replaceStringKeys(
                htmlContent, Collections.singletonList(key))
        ).isEqualTo(htmlContent.replace(key, "mail.message.index:0"));
    }

}