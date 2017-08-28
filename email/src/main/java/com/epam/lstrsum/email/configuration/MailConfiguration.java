package com.epam.lstrsum.email.configuration;

import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.mail.ImapIdleChannelAdapter;
import org.springframework.integration.mail.ImapMailReceiver;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

@Configuration
@ConfigurationProperties(prefix = "email")
@Profile("email")
public class MailConfiguration {
    @Setter
    @Value("${spring.mail.username}")
    private String userName;

    @Setter
    @Value("${spring.mail.password}")
    private String password;

    @Setter
    private String imapServer;

    @Setter
    private String imapServerPort;

    @Setter
    private String folder;

    @Bean
    public QueueChannel receiveChannel() {
        return new QueueChannel();
    }

    @Bean
    public ImapIdleChannelAdapter imapIdleChannelAdapter(ImapMailReceiver imapMailReceiver) {
        ImapIdleChannelAdapter imapIdleChannelAdapter = new ImapIdleChannelAdapter(imapMailReceiver);
        imapIdleChannelAdapter.setAutoStartup(true);
        imapIdleChannelAdapter.setOutputChannel(receiveChannel());

        return imapIdleChannelAdapter;
    }

    @Bean
    public ImapMailReceiver imapMailReceiver() throws UnsupportedEncodingException {
        ImapMailReceiver receiver = new ImapMailReceiver(
                "imaps://" + URLEncoder.encode(userName, "UTF-8") + ":" + password + "@" + imapServer + ":" + imapServerPort + "/" + folder
        );
        receiver.setShouldDeleteMessages(false);
        receiver.setShouldMarkMessagesAsRead(true);
        return receiver;
    }
}
