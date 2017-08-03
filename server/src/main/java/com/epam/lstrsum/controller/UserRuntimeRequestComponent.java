package com.epam.lstrsum.controller;

import com.epam.lstrsum.security.EpamEmployeePrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@Component
@RequestScope
@Slf4j
@RequiredArgsConstructor
public class UserRuntimeRequestComponent {
    private final HttpServletRequest request;

    public String getEmail() {
        return getPrincipal().getEmail();
    }

    private EpamEmployeePrincipal getPrincipal() {
        return Optional.ofNullable(request.getUserPrincipal())
                .map(o -> (OAuth2Authentication)o)
                .map(u -> (EpamEmployeePrincipal) u.getPrincipal())
                .orElseGet(() -> {
                    log.warn("Unsecured invocation detected");
                    return EpamEmployeePrincipal.builder().email("John_Doe@epam.com").build();
                });
    }
}

