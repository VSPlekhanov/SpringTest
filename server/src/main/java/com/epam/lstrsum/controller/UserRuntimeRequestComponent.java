package com.epam.lstrsum.controller;

import com.epam.lstrsum.security.EpamEmployeePrincipal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import javax.servlet.http.HttpServletRequest;

import static java.util.Objects.isNull;

@Component
@RequestScope
@Slf4j
public class UserRuntimeRequestComponent {

    private final HttpServletRequest request;

    @Autowired
    public UserRuntimeRequestComponent(HttpServletRequest request) {
        this.request = request;
    }

    public String getEmail() {
        return getPrincipal().getEmail();
    }

    private EpamEmployeePrincipal getPrincipal() {
        OAuth2Authentication authentication = (OAuth2Authentication) request.getUserPrincipal();


        if (isNull(authentication)) {
            log.warn("Unsecured invocation detected");
            EpamEmployeePrincipal epamEmployeePrincipal = new EpamEmployeePrincipal();
            epamEmployeePrincipal.setEmail("John_Doe@epam.com");
            return epamEmployeePrincipal;
        }
        return (EpamEmployeePrincipal) authentication.getPrincipal();
    }
}

