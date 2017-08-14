package com.epam.lstrsum.service.mail;

import com.epam.lstrsum.SetUpDataBaseCollections;
import com.epam.lstrsum.email.service.ExchangeServiceHelper;
import com.epam.lstrsum.model.User;
import com.epam.lstrsum.service.UserService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
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

    @Autowired
    private UserService userService;

    @Autowired
    private UserSynchronizer userSynchronizer;

    @Test
    public void synchronizeUsers() throws Exception {
        final String bobHoplins = "Bob_Hoplins@epam.com";
        final String johnDoe = "John_Doe@epam.com";

        userService.setActiveForAllAs(Collections.singleton(johnDoe), false);
        doReturn(Arrays.asList(johnDoe, bobHoplins))
                .when(exchangeServiceHelper).resolveGroup(anyString());

        userSynchronizer.synchronizeUsers();

        assertThat(userService.findUserByEmail(johnDoe).getIsActive())
                .isTrue();
        assertThat(userService.findUserByEmail(bobHoplins).getIsActive())
                .isTrue();

        assertThat(userService.findAllWithRole("USER").stream().filter(User::getIsActive))
                .hasSize(2);
        assertThat(userService.findAllWithRole("USER").stream().filter(u -> !u.getIsActive()))
                .hasSize(4);

    }

}