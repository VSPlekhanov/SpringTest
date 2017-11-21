package com.epam.lstrsum.email.configuration;


import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.enumeration.misc.ExchangeVersion;
import microsoft.exchange.webservices.data.credential.ExchangeCredentials;
import microsoft.exchange.webservices.data.credential.WebCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;

import static java.util.Objects.isNull;

@Configuration
@Slf4j
public class ExchangeConfiguration {
    @Setter
    @Value("${email.exchangeServer}")
    private String exchangeServer;

    @Setter
    @Value("${spring.mail.username}")
    private String userName;

    @Setter
    @Value("${spring.mail.password}")
    private String password;

    @Bean(destroyMethod = "close")
    public ExchangeService exchangeService() {
        final ExchangeService service = new ExchangeService(ExchangeVersion.Exchange2010_SP2);
        if (isNull(userName) || isNull(password)) {
            return service;
        }

        final ExchangeCredentials credentials = new WebCredentials(userName, password);
        service.setCredentials(credentials);

        try {
            service.setUrl(new URI(exchangeServer));
        } catch (final Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }

        log.debug("Set exchange server: {}", exchangeServer);

        return service;
    }
}
