package com.epam.lstrsum.email.service;

import org.junit.Test;

import javax.mail.Address;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
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

}