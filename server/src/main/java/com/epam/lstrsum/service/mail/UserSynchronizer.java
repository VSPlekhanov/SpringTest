package com.epam.lstrsum.service.mail;


import com.epam.lstrsum.email.service.ExchangeServiceHelper;
import com.epam.lstrsum.enums.UserRoleType;
import com.epam.lstrsum.model.User;
import com.epam.lstrsum.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserSynchronizer {
    private static final List<UserRoleType> ONLY_COMMON_USER_ROLES = Collections.singletonList(UserRoleType.EXTENDED_USER);
    private final ExchangeServiceHelper exchangeServiceHelper;
    private final UserService userService;

    @Setter
    @Value("${email.distribution-list}")
    private String distributionList;

    @Scheduled(cron = "0 0 9 * * *")
    public void synchronizeUsers() {
        log.debug("Start synchronizing users");

        final List<User> allWithRole = userService.findAllWithRole(UserRoleType.EXTENDED_USER);
        final Set<String> activeUsers = allWithRole.stream()
                .filter(u -> nonNull(u.getIsActive()))
                .filter(User::getIsActive)
                .map(User::getEmail)
                .collect(Collectors.toSet());

        final Set<String> notActiveUsers = allWithRole.stream()
                .filter(u -> isNull(u.getIsActive()) || !u.getIsActive())
                .map(User::getEmail)
                .collect(Collectors.toSet());

        List<String> emails = exchangeServiceHelper.resolveGroup(distributionList);

        activeUsers.removeAll(emails);
        final long deactivated = userService.setActiveForAllAs(activeUsers, false);

        notActiveUsers.retainAll(emails);
        final long activated = userService.setActiveForAllAs(notActiveUsers, true);

        final long userAdded = userService.addIfNotExistAllWithRole(emails, ONLY_COMMON_USER_ROLES);

        log.debug("added {} users, activated {} users, deactivated users {}", userAdded, activated, deactivated);
    }

}
