package com.epam.lstrsum.service.mail;

import com.epam.lstrsum.SetUpDataBaseCollections;
import com.epam.lstrsum.email.service.ExchangeServiceHelper;
import com.epam.lstrsum.enums.UserRoleType;
import com.epam.lstrsum.model.User;
import com.epam.lstrsum.service.TelescopeService;
import com.epam.lstrsum.service.UserService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;

import static com.epam.lstrsum.testutils.InstantiateUtil.someTelescopeEmployeeEntityDtosWithEmails;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anySetOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;

/**
 * experience
 * Created on 11.08.17.
 */

@ActiveProfiles("email")
public class UserSynchronizerTest extends SetUpDataBaseCollections {
    @MockBean
    private ExchangeServiceHelper exchangeServiceHelper;

    @MockBean
    private TelescopeService telescopeService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserSynchronizer userSynchronizer;

    @Test
    public void isInDistributionList() {
        assertThat(userSynchronizer.isInDistributionList("John_Doe@epam.com")).isTrue();
        assertThat(userSynchronizer.isInDistributionList("Bob_Hoplins@epam.com")).isTrue();
        assertThat(userSynchronizer.isInDistributionList("Tyler_Greeds@epam.com")).isFalse();
        assertThat(userSynchronizer.isInDistributionList("Donald_Gardner@epam.com")).isFalse();
        assertThat(userSynchronizer.isInDistributionList("Ernest_Hemingway@epam.com")).isFalse();
        assertThat(userSynchronizer.isInDistributionList("Steven_Tyler@epam.com")).isFalse();
        assertThat(userSynchronizer.isInDistributionList("no_such_email@epam.com")).isFalse();
    }

    @Test
    public void synchronizeUsers() throws Exception {
        final String bobHoplins = "Bob_Hoplins@epam.com";
        final String johnDoe = "John_Doe@epam.com";

        doReturn(someTelescopeEmployeeEntityDtosWithEmails(bobHoplins, johnDoe))
                .when(telescopeService).getUsersInfoByEmails(anySetOf(String.class));
        doReturn(Arrays.asList(johnDoe, bobHoplins))
                .when(exchangeServiceHelper).resolveGroup(anyString());

        userSynchronizer.synchronizeUsers();

        assertThat(userService.findUserByEmail(johnDoe).getIsActive())
                .isTrue();
        assertThat(userService.findUserByEmail(bobHoplins).getIsActive())
                .isTrue();

        assertThat(userService.findAllWithRole(UserRoleType.ROLE_EXTENDED_USER).stream().filter(User::getIsActive))
                .hasSize(2);
        assertThat(userService.findAllWithRole(UserRoleType.ROLE_EXTENDED_USER).stream().filter(u -> !u.getIsActive()))
                .hasSize(3);

    }

}