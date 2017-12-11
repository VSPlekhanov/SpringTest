package com.epam.lstrsum.security;

import com.epam.lstrsum.model.User;
import com.epam.lstrsum.security.role.RoleService;
import com.epam.lstrsum.service.UserService;
import com.epam.lstrsum.utils.MessagesHelper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

import static com.epam.lstrsum.enums.UserRoleType.ROLE_SIMPLE_USER;

@Slf4j
@RequiredArgsConstructor
public class CustomResourceServerTokenServices implements ResourceServerTokenServices {
    private final RoleService roleService;
    private final AuthorizationCodeResourceDetails authorizationCodeResourceDetails;
    private final UserService userService;
    private final MessagesHelper messagesHelper;

    @Override
    public OAuth2Authentication loadAuthentication(String accessToken) throws AuthenticationException, InvalidTokenException {

        try {
            Jwt jwt = JwtHelper.decode(accessToken);
            String jwtClaims = jwt.getClaims();
            Map<String, Object> map = new ObjectMapper().readValue(jwtClaims, new TypeReference<Map<String, String>>() {});

            OAuth2Request request = new OAuth2Request(null, authorizationCodeResourceDetails.getClientId(),
                    null, true, null,
                    null, null, null, null);

            String email = getEmailFromMap(map);

            val user = handleUserFromToken(email);
            map.put(EpamEmployeePrincipal.DISTRIBUTION_LIST_USER, user.map(User::getIsActive).orElse(false));

            EpamEmployeePrincipal principal = EpamEmployeePrincipal.ofMap(map);
            final UsernamePasswordAuthenticationToken finalToken =
                    new UsernamePasswordAuthenticationToken(principal, "N/A",
                            AuthorityUtils.createAuthorityList(
                                    user.map(roleService::getPrincipalRoles).orElse(
                                            new String[]{ROLE_SIMPLE_USER.name()})
                            )
                    );

            return new OAuth2Authentication(request, finalToken);
        } catch (AuthenticationException | InvalidTokenException e) {
            log.error("Token process exception: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Token process exception: {}", e.getMessage());
            throw new InvalidTokenException(e.getMessage(), e);
        }
    }

    private String getEmailFromMap(Map<String, Object> map) {
        return Optional.ofNullable(map.get(EpamEmployeePrincipal.EMAIL))
                .map(o -> (String) o)
                .orElseThrow(() -> new IllegalArgumentException(messagesHelper.get("validation.security.wrong-map-format")));
    }

    private Optional<User> handleUserFromToken(String email) {
        Optional<User> user = userService.findUserByEmailIfExist(email);
        if (!user.isPresent()) log.info("User with email : '{}' visit portal. This user is not in a db.", email);
        return user;
    }

    public OAuth2AccessToken readAccessToken(String s) {
        throw new UnsupportedOperationException(messagesHelper.get("validation.security.not-supported"));
    }
}