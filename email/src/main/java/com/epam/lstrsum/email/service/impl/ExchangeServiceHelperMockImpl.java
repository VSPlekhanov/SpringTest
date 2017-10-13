package com.epam.lstrsum.email.service.impl;

import com.epam.lstrsum.email.service.ExchangeServiceHelper;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
@Profile("standalone")
@Primary
@RequiredArgsConstructor
public class ExchangeServiceHelperMockImpl implements ExchangeServiceHelper {

    private final CounterService counterService;
    @Value("${email.distribution-list}")
    @Setter
    private String distributionList;

    @Override
    public List<String> resolveGroup(String groupName) {
        counterService.increment("exchange.service.expand.group");
        if (distributionList.equalsIgnoreCase(groupName)) {
            return Arrays.asList("rustam_kadyrov@epam.com",
                    "anna_davydova@epam.com",
                    "gleb_strus@epam.com",
                    "kirill_gavrilov@epam.com",
                    "daria_makarova@epam.com",
                    "elizaveta_tomaeva@epam.com",
                    "nikita_zaitcev@epam.com",
                    "maksim_nikolaev@epam.com",
                    "aleksandr_shevkunenko@epam.com",
                    "anastasiia_turenko@epam.com",
                    "evgenii_kurdakov@epam.com");
        } else {
            return Collections.singletonList(groupName);
        }
    }
}
