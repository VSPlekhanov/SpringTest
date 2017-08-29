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
            return Arrays.asList("Rustam_Kadyrov@epam.com",
                    "Anna_Davydova@epam.com",
                    "Gleb_Strus@epam.com",
                    "Kirill_Gavrilov@epam.com",
                    "Vyacheslav_Lapin@epam.com",
                    "Mikhail_Figurin@epam.com",
                    "Ivan_Chuvakhin@epam.com",
                    "Evgenii_Aleksandrov@epam.com",
                    "Egor_Zakovriashin@epam.com",
                    "Igor_Siluianov@epam.com",
                    "Ekaterina_Son@epam.com",
                    "Daria_Makarova@epam.com",
                    "Igor_Drozdov1@epam.com",
                    "Aleksei_Chepuko@epam.com",
                    "Elizaveta_Tomaeva@epam.com",
                    "Anton_Nazarov1@epam.com");
        } else {
            return Collections.singletonList(groupName);
        }
    }
}
