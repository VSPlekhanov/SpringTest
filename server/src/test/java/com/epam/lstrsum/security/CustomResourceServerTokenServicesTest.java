package com.epam.lstrsum.security;

import com.epam.lstrsum.enums.UserRoleType;
import com.epam.lstrsum.model.User;
import com.epam.lstrsum.security.role.RoleService;
import com.epam.lstrsum.service.UserService;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;

import java.util.Optional;

import static com.epam.lstrsum.testutils.InstantiateUtil.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class CustomResourceServerTokenServicesTest {
    private static final String ROLE_EXTENDED_USER = UserRoleType.ROLE_EXTENDED_USER.name();
    private static final String ROLE_SIMPLE_USER = UserRoleType.ROLE_SIMPLE_USER.name();
    private static final String SOME_CLIENT_ID = someString();

    private static final String VALID_JWT_ACCESS_TOKEN =
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1cG4iOiIxdSIsInVuaXF1ZV9uYW1lIjoiSm9obiBEb2UiLCJlbWFpbCI6IkpvaG5fRG9lQGVwYW0uY29tIn0.O0ZtGk6YM8PV6sX76RwvOrExZpZRf3XNovR-uscdIgw";

    private static final String JWT_ACCESS_TOKEN_WITH_NO_SIGNATURE =
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
        when(authorizationCodeResourceDetails.getClientId()).thenReturn(EXISTING_USER_ID);
        when(roleService.getPrincipalRoles(any(User.class))).thenReturn(new String[]{ROLE_EXTENDED_USER});
        val someActiveUser = someActiveUser();
        doReturn(Optional.of(someActiveUser)).when(userService).findUserByEmailIfExist(any(String.class));

        OAuth2Authentication auth = tokenServices.loadAuthentication(VALID_JWT_ACCESS_TOKEN);
        EpamEmployeePrincipal employeePrincipal = (EpamEmployeePrincipal) auth.getPrincipal();

        assertNotNull(auth);

        assertEquals(auth.getOAuth2Request().getClientId(), EXISTING_USER_ID);

        assertThat(employeePrincipal.getUserId(), is(EXISTING_USER_ID));
        assertThat(employeePrincipal.getName(), is(SOME_USER_NAME));
        assertThat(employeePrincipal.getEmail(), is(SOME_USER_EMAIL));

        verify(authorizationCodeResourceDetails, times(1)).getClientId();
        verify(roleService, times(1)).getPrincipalRoles(any(User.class));
        verify(userService, times(1)).findUserByEmailIfExist(any(String.class));
    }

    @Test
    public void successLoadAuthenticationReturningNotActiveUser() {
        when(authorizationCodeResourceDetails.getClientId()).thenReturn(SOME_CLIENT_ID);
        val someNotActiveUser = someNotActiveUser();
        doReturn(Optional.of(someNotActiveUser)).when(userService).findUserByEmailIfExist(any(String.class));
        when(roleService.getPrincipalRoles(any(User.class))).thenReturn(new String[]{ROLE_EXTENDED_USER});

        tokenServices.loadAuthentication(VALID_JWT_ACCESS_TOKEN);

        verify(authorizationCodeResourceDetails, times(1)).getClientId();
        verify(userService, times(1)).findUserByEmailIfExist(any(String.class));
        verify(roleService, times(1)).getPrincipalRoles(any(User.class));
    }

    @Test
    public void successLoadAuthenticationByNotExistInDBUser() {
        when(authorizationCodeResourceDetails.getClientId()).thenReturn(someString());
        doReturn(Optional.empty()).when(userService).findUserByEmailIfExist(any(String.class));

        OAuth2Authentication authentication = tokenServices.loadAuthentication(VALID_JWT_ACCESS_TOKEN);

        verify(authorizationCodeResourceDetails, times(1)).getClientId();
        verify(userService, times(1)).findUserByEmailIfExist(any(String.class));
        verify(roleService, times(0)).getPrincipalRoles(any(User.class));
        assertThat(authentication.getAuthorities().iterator().next().toString(), is(ROLE_SIMPLE_USER));
    }

    @Test(expected = InvalidTokenException.class)
    public void loadAuthenticationWithNullAccessToken() {
        tokenServices.loadAuthentication(null);
    }

    @Test(expected = InvalidTokenException.class)
    public void loadAuthenticationWithEmptyAccessToken() {
        tokenServices.loadAuthentication("          ");
    }

    @Test(expected = InvalidTokenException.class)
    public void loadAuthenticationWithNoSignatureAccessToken() {
        tokenServices.loadAuthentication(JWT_ACCESS_TOKEN_WITH_NO_SIGNATURE);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void checkThatReadAccessTokenThrowException() {
        tokenServices.readAccessToken(VALID_JWT_ACCESS_TOKEN);
    }
}
