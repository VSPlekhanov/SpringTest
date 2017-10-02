package com.epam.lstrsum.security;

import com.epam.lstrsum.exception.NoSuchUserException;
import com.epam.lstrsum.model.User;
import com.epam.lstrsum.security.role.RoleService;
import com.epam.lstrsum.service.UserService;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;

import static com.epam.lstrsum.testutils.InstantiateUtil.someActiveUser;
import static com.epam.lstrsum.testutils.InstantiateUtil.someNotActiveUser;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class CustomResourceServerTokenServicesTest {
    private static final String ROLE_USER = "ROLE_USER";
    private static final String SOME_CLIENT_ID = "7643";
    private static final String SOME_EMAIL = "email@test.com";
    private static final String SOME_UNIQUE_NAME = "John Doe";
    private static final String SOME_USER_ID = "1";


    private static final String VALID_ACCESS_TOKEN =
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1cG4iOiIxIiwidW5pcXVlX25hbWUiOiJKb2huIERvZSIsImVtYWlsIjoiZW1haWxAdGVzdC5jb20ifQ" +
                    ".VoZ16tq1IBirXoMaMPvSHSl7z_PVQjDOTAKiVkJTMsY";

    private static final String ACCESS_TOKEN_WITH_NO_SIGNATURE =
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1cG4iOiIxIiwidW5pcXVlX25hbWUiOiJKb2huIERvZSIsImVtYWlsIjoiZW1haWxAdGVzdC5jb20ifQ";


    @Mock
    private RoleService roleService;

    @Mock
    private AuthorizationCodeResourceDetails authorizationCodeResourceDetails;

    @Mock
    private UserService userService;

    private ResourceServerTokenServices tokenServices;

    @Before
    public void setUp() throws Exception {
        initMocks(this);

        tokenServices = new CustomResourceServerTokenServices(
                roleService, authorizationCodeResourceDetails, userService
        );
    }

    @Test
    public void successLoadAuthenticationReturningActiveUser() {
        when(authorizationCodeResourceDetails.getClientId()).thenReturn(SOME_CLIENT_ID);
        when(roleService.getPrincipalRoles(any(User.class))).thenReturn(new String[]{ROLE_USER});
        val someActiveUser = someActiveUser();
        when(userService.findUserByEmail(any(String.class))).thenReturn(someActiveUser);

        OAuth2Authentication auth = tokenServices.loadAuthentication(VALID_ACCESS_TOKEN);
        EpamEmployeePrincipal employeePrincipal = (EpamEmployeePrincipal) auth.getPrincipal();

        assertNotNull(auth);

        assertEquals(auth.getOAuth2Request().getClientId(), SOME_CLIENT_ID);

        assertThat(employeePrincipal.getUserId(), is(SOME_USER_ID));
        assertThat(employeePrincipal.getName(), is(SOME_UNIQUE_NAME));
        assertThat(employeePrincipal.getEmail(), is(SOME_EMAIL));

        verify(authorizationCodeResourceDetails, times(1)).getClientId();
        verify(roleService, times(1)).getPrincipalRoles(any(User.class));
        verify(userService, times(1)).findUserByEmail(any(String.class));
    }

    @Test
    public void successLoadAuthenticationReturningNotActiveUser() {
        when(authorizationCodeResourceDetails.getClientId()).thenReturn(SOME_CLIENT_ID);
        val someNotActiveUser = someNotActiveUser();
        when(userService.findUserByEmail(any(String.class))).thenReturn(someNotActiveUser);
        when(roleService.getPrincipalRoles(any(User.class))).thenReturn(new String[]{ROLE_USER});

        tokenServices.loadAuthentication(VALID_ACCESS_TOKEN);

        verify(authorizationCodeResourceDetails, times(1)).getClientId();
        verify(userService, times(1)).findUserByEmail(any(String.class));
        verify(roleService, times(1)).getPrincipalRoles(any(User.class));
    }

    @Test
    public void successLoadAuthenticationThrowingException() {
        when(authorizationCodeResourceDetails.getClientId()).thenReturn(SOME_CLIENT_ID);
        final NoSuchUserException cause = new NoSuchUserException("No such User in user Collection");
        when(userService.findUserByEmail(any(String.class))).thenThrow(cause);

        assertThatThrownBy(() -> tokenServices.loadAuthentication(VALID_ACCESS_TOKEN))
                .hasMessage("No such User in user Collection")
                .isInstanceOf(AuthenticationServiceException.class);

        verify(authorizationCodeResourceDetails, times(1)).getClientId();
        verify(userService, times(1)).findUserByEmail(any(String.class));
    }

    @Test(expected = AuthenticationServiceException.class)
    public void loadAuthenticationWithNullAccessToken() {
        tokenServices.loadAuthentication(null);
    }

    @Test(expected = AuthenticationServiceException.class)
    public void loadAuthenticationWithEmptyAccessToken() {
        tokenServices.loadAuthentication("          ");
    }

    @Test(expected = AuthenticationServiceException.class)
    public void loadAuthenticationWithNoSignatureAccessToken() {
        tokenServices.loadAuthentication(ACCESS_TOKEN_WITH_NO_SIGNATURE);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void checkThatReadAccessTokenThrowException() {
        tokenServices.readAccessToken(VALID_ACCESS_TOKEN);
    }
}
