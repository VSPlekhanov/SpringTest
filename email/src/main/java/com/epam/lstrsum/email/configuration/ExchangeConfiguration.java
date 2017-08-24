package com.epam.lstrsum.email.configuration;


import lombok.Setter;
import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.enumeration.misc.ExchangeVersion;
import microsoft.exchange.webservices.data.credential.ExchangeCredentials;
import microsoft.exchange.webservices.data.credential.WebCredentials;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;

import static java.util.Objects.isNull;

@Configuration
public class ExchangeConfiguration {
    @Setter
    private String exchangeServer;

    @Setter
    private String userName;

    @Setter
    private String password;

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
