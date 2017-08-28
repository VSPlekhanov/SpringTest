package com.epam.lstrsum.configuration;

import microsoft.exchange.webservices.data.core.ExchangeService;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExchangeServiceTestConfiguration {

    @MockBean
    private ExchangeService exchangeService;

}
