package com.epam.lstrsum.email.configuration.mock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.mail.ImapIdleChannelAdapter;
import org.springframework.integration.mail.ImapMailReceiver;

import java.io.UnsupportedEncodingException;

@Configuration
@Profile("standalone")
public class MailConfigurationMock {

    @Bean
    @Primary
    public ImapIdleChannelAdapter imapIdleChannelAdapter(ImapMailReceiver imapMailReceiver) {
        return null;
    }

    @Bean
    @Primary
    public ImapMailReceiver imapMailReceiver() throws UnsupportedEncodingException {
        return null;
    }
}
