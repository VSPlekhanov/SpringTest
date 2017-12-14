package com.epam.lstrsum.controller;

import com.epam.lstrsum.model.User;
import com.epam.lstrsum.security.EpamEmployeePrincipal;
import com.epam.lstrsum.service.UserService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class UserRuntimeQuestionComponentTest {

    @Mock
    private HttpServletRequest httpRequest;

    @Mock
    private OAuth2Authentication oAuth2Authentication;

    @Mock
    private UserService userService;

    private UserRuntimeRequestComponent userRuntimeRequestComponent;

    @Before
    public void setUp() {
        initMocks(this);
        userRuntimeRequestComponent = new UserRuntimeRequestComponent(httpRequest);
        userRuntimeRequestComponent.setUserService(userService);
    }

    @Test
    public void getPrincipalIsInDistributionListCorrect() {
        when(userService.findUserByEmailIfExist("email")).thenReturn(Optional.of(userActive()));
        when(httpRequest.getUserPrincipal()).thenReturn(oAuth2Authentication);
        EpamEmployeePrincipal expectedPrincipal = epamEmployeePrincipal();
        expectedPrincipal.setUserInDistributionList(true);
        when(oAuth2Authentication.getPrincipal()).thenReturn(expectedPrincipal);

        assertEquals(true, userRuntimeRequestComponent.isInDistributionList());

        when(userService.findUserByEmailIfExist("email")).thenReturn(Optional.of(userNotActive()));
        assertNotEquals(true, userRuntimeRequestComponent.isInDistributionList());
    }

    @Test
    public void getEmailCorrect() throws Exception {
        when(userService.findUserByEmailIfExist("email")).thenReturn(Optional.of(userActive()));
        when(httpRequest.getUserPrincipal()).thenReturn(oAuth2Authentication);
        EpamEmployeePrincipal expectedPrincipal = epamEmployeePrincipal();
        when(oAuth2Authentication.getPrincipal()).thenReturn(expectedPrincipal);

        String actual = userRuntimeRequestComponent.getEmail();

        assertEquals(expectedPrincipal.getEmail(), actual);
    }

    @Test
    public void getEmailUnsecuredConnection() {
        when(userService.findUserByEmailIfExist("John_Doe@epam.com")).thenReturn(Optional.of(userActive()));
        String actual = userRuntimeRequestComponent.getEmail();
        assertEquals("John_Doe@epam.com", actual);
    }

    private EpamEmployeePrincipal epamEmployeePrincipal() {
        Map<String, Object> epamEmployeePrincipalMap = new HashMap<>();
        epamEmployeePrincipalMap.put(EpamEmployeePrincipal.USER_ID, "userId");
        epamEmployeePrincipalMap.put(EpamEmployeePrincipal.UNIQUE_NAME, "uniqueName");
        epamEmployeePrincipalMap.put(EpamEmployeePrincipal.EMAIL, "email");

        return EpamEmployeePrincipal.ofMap(epamEmployeePrincipalMap);
    }

    private User userActive() {
        return User.builder().isActive(true).build();
    }

    private User userNotActive() {
        return User.builder().isActive(false).build();
    }
}