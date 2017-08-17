package com.epam.lstrsum.service.mail;

import com.epam.lstrsum.email.service.ExchangeServiceHelper;
import com.epam.lstrsum.email.service.ExchangeServiceHelperImpl;
import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.misc.ExpandGroupResults;
import microsoft.exchange.webservices.data.property.complex.EmailAddress;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * experience
 * Created on 10.08.17.
 */
public class ExchangeServiceHelperImplTest {
    @Mock
    private ExchangeService exchangeService;

    private ExchangeServiceHelper exchangeServiceHelper;

    private static ExpandGroupResults generateByStringList(Collection<String> emails) {
        final ExpandGroupResults result = new ExpandGroupResults();
        result.getMembers().addAll(emails.stream()
                .map(EmailAddress::new)
                .collect(Collectors.toList()));

        return result;
    }

    @Before
    public void setUp() {
        initMocks(this);

        exchangeServiceHelper = new ExchangeServiceHelperImpl(exchangeService);
    }

    @Test
    public void resolveGroupSingleResponse() throws Exception {
        final String someAddress = "address";
        doReturn(generateByStringList(singletonList(someAddress))).when(exchangeService).expandGroup(anyString());

        assertThat(exchangeServiceHelper.resolveGroup("someGroup"))
                .containsOnly(someAddress);
    }

    @Test
    public void resolveGroupSingleResponseWithException() throws Exception {
        doThrow(Exception.class).when(exchangeService).expandGroup(anyString());

        final String someGroup = "someGroup";
        assertThat(exchangeServiceHelper.resolveGroup(someGroup))
                .containsOnly(someGroup);
    }

    @Test
    public void resolveGroupSecondLevelDeep() throws Exception {
        doReturn(generateByStringList(Arrays.asList("1", "2", "3"))).when(exchangeService).expandGroup("1");
        doReturn(generateByStringList(singletonList("4"))).when(exchangeService).expandGroup("2");
        doReturn(generateByStringList(singletonList("5"))).when(exchangeService).expandGroup("3");

        assertThat(exchangeServiceHelper.resolveGroup("1"))
                .containsOnly("1", "4", "5");
    }

    @Test
    public void resolveGroupThirdLevelDeep() throws Exception {
        doReturn(generateByStringList(Arrays.asList("1", "2", "3"))).when(exchangeService).expandGroup("1");
        doReturn(generateByStringList(Arrays.asList("4", "5"))).when(exchangeService).expandGroup("2");
        doReturn(generateByStringList(Arrays.asList("6", "7"))).when(exchangeService).expandGroup("3");
        doThrow(Exception.class).when(exchangeService).expandGroup("4");
        doThrow(Exception.class).when(exchangeService).expandGroup("5");
        doThrow(Exception.class).when(exchangeService).expandGroup("6");
        doThrow(Exception.class).when(exchangeService).expandGroup("7");

        assertThat(exchangeServiceHelper.resolveGroup("1"))
                .containsOnly("1", "4", "5", "6", "7");

    }

}