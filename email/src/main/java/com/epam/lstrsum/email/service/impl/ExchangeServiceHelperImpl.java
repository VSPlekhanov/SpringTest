package com.epam.lstrsum.email.service.impl;

import com.epam.lstrsum.email.service.ExchangeServiceHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.property.complex.EmailAddress;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * experience
 * Created on 10.08.17.
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class ExchangeServiceHelperImpl implements ExchangeServiceHelper {
    private final ExchangeService exchangeService;

    @Override
    public List<String> resolveGroup(final String groupName) {
        final List<String> result = new ArrayList<>();

        internalRecursiveResolving(result, groupName);

        return result;
    }

    private void internalRecursiveResolving(final List<String> result, final String groupName) {
        final List<String> addresses = simpleInternalExpand(groupName);
        if (addresses.size() == 1) {
            result.addAll(addresses);
        } else {
            result.add(addresses.get(0));
            addresses.subList(1, addresses.size()).forEach(el -> internalRecursiveResolving(result, el));
        }
    }


    private List<String> simpleInternalExpand(final String groupName) {
        try {
            return exchangeService.expandGroup(groupName).getMembers().stream()
                    .map(EmailAddress::getAddress)
                    .collect(Collectors.toList());
        } catch (final Exception e) {
            return Collections.singletonList(groupName);
        }
    }
}
