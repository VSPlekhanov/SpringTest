package com.epam.lstrsum.configuration;

import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.mail.ImapIdleChannelAdapter;
import org.springframework.integration.mail.ImapMailReceiver;

import java.util.Properties;

@Configuration
@ConfigurationProperties(prefix = "mail")
public class MailConfiguration {
    @Setter
    private String userName;

    @Setter
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
    public ImapIdleChannelAdapter imapIdleChannelAdapter() {
        ImapIdleChannelAdapter imapIdleChannelAdapter = new ImapIdleChannelAdapter(imapMailReceiver());
        imapIdleChannelAdapter.setAutoStartup(true);
        imapIdleChannelAdapter.setOutputChannel(receiveChannel());

        return imapIdleChannelAdapter;
    }

    @Bean
    public ImapMailReceiver imapMailReceiver() {
        ImapMailReceiver receiver = new ImapMailReceiver(
                "imaps://" + userName + ":" + password + "@" + imapServer + ":" + imapServerPort + "/" + folder
        );
        receiver.setShouldDeleteMessages(false);
        receiver.setShouldMarkMessagesAsRead(true);
        receiver.setJavaMailProperties(javaMailProperties());
        return receiver;
    }

    @Bean
    public Properties javaMailProperties() {
        Properties properties = new Properties();
        properties.setProperty("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        properties.setProperty("mail.imap.socketFactory.fallback", "false");
        properties.setProperty("mail.store.protocol", "imaps");
        properties.setProperty("mail.debug", "false");
        return properties;
    }
}
