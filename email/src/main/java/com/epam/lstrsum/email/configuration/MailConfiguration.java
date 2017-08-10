package com.epam.lstrsum.email.configuration;

import lombok.Setter;
import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.enumeration.misc.ExchangeVersion;
import microsoft.exchange.webservices.data.credential.ExchangeCredentials;
import microsoft.exchange.webservices.data.credential.WebCredentials;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.mail.ImapIdleChannelAdapter;
import org.springframework.integration.mail.ImapMailReceiver;

import java.net.URI;

import static java.util.Objects.isNull;

@Configuration
@ConfigurationProperties(prefix = "email")
@Profile("email")
public class MailConfiguration {

    @Setter
    private String exchangeServer;

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
        return receiver;
    }

    @Bean(destroyMethod = "close")
    public ExchangeService exchangeService() {
        final ExchangeService service = new ExchangeService(ExchangeVersion.Exchange2010_SP2);
        if (isNull(userName) || isNull(password)) {
            return service;
        }

        final ExchangeCredentials credentials = new WebCredentials(userName.replace("%40", "@"), password);
        service.setCredentials(credentials);

        try {
            service.setUrl(new URI(exchangeServer));
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }

        return service;
    }
}
