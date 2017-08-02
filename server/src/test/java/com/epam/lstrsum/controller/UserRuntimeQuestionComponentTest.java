package com.epam.lstrsum.controller;

import com.epam.lstrsum.security.EpamEmployeePrincipal;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class UserRuntimeQuestionComponentTest {

    @Mock
    private HttpServletRequest httpRequest;

    @Mock
    private OAuth2Authentication oAuth2Authentication;

    private UserRuntimeRequestComponent userRuntimeRequestComponent;

    @Before
    public void setUp() {
        initMocks(this);
        userRuntimeRequestComponent = new UserRuntimeRequestComponent(httpRequest);
    }

    @Test
    public void getEmailCorrect() throws Exception {
        when(httpRequest.getUserPrincipal()).thenReturn(oAuth2Authentication);
        EpamEmployeePrincipal expectedPrincipal = epamEmployeePrincipal();
        when(oAuth2Authentication.getPrincipal()).thenReturn(expectedPrincipal);

        String actual = userRuntimeRequestComponent.getEmail();

        assertEquals(expectedPrincipal.getEmail(), actual);
    }

    @Test
    public void getEmailUnsecuredConnection() {
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

}