package com.epam.lstrsum.security;

import com.epam.lstrsum.security.role.RoleService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;

import java.util.Map;

@Slf4j
public class CustomResourceServerTokenServices implements ResourceServerTokenServices {

    private final AuthorizationCodeResourceDetails authorizationCodeResourceDetails;

    private final RoleService roleService;

    public CustomResourceServerTokenServices(RoleService roleService, AuthorizationCodeResourceDetails authorizationCodeResourceDetails) {
        this.roleService = roleService;
        this.authorizationCodeResourceDetails = authorizationCodeResourceDetails;
    }

    public OAuth2Authentication loadAuthentication(String accessToken) throws AuthenticationException, InvalidTokenException {

        try {
            Jwt jwt = JwtHelper.decode(accessToken);
            String jwtClaims = jwt.getClaims();
            Map<String, Object> map = new ObjectMapper().readValue(jwtClaims, new TypeReference<Map<String, String>>() {
            });
            EpamEmployeePrincipal principle = EpamEmployeePrincipal.ofMap(map);

            OAuth2Request request = new OAuth2Request(null, authorizationCodeResourceDetails.getClientId(),
                    null, true, null,
                    null, null, null, null);

            final UsernamePasswordAuthenticationToken finalToken =
                    new UsernamePasswordAuthenticationToken(principle, "N/A", AuthorityUtils.createAuthorityList(roleService.getPrincipalRoles(principle)));

            return new OAuth2Authentication(request, finalToken);
        } catch (Exception e) {
            log.error("Token process exception!", e);
            throw new AuthenticationServiceException(e.getMessage(), e);
        }
    }

    public OAuth2AccessToken readAccessToken(String s) {
        throw new UnsupportedOperationException("Not supported.");
    }
}