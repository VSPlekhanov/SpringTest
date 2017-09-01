package com.epam.lstrsum.email.configuration.mock;

import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.enumeration.misc.ExchangeVersion;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Profile("standalone")
public class ExchangeConfigurationMock {

    @Bean(destroyMethod = "close")
    @Primary
    public ExchangeService exchangeService() {
        return new ExchangeService(ExchangeVersion.Exchange2010_SP2);
    }
}
