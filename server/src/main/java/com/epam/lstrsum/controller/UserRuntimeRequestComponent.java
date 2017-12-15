package com.epam.lstrsum.controller;

import com.epam.lstrsum.model.User;
import com.epam.lstrsum.security.EpamEmployeePrincipal;
import com.epam.lstrsum.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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

    @Autowired
    @Setter
    private UserService userService;

    public String getEmail() {
        return getPrincipal().getEmail();
    }

    public boolean isInDistributionList() {
        return getPrincipal().isUserInDistributionList();
    }

    private EpamEmployeePrincipal getPrincipal() {
        log.debug("getPrincipal.enter; request {}", request);
        EpamEmployeePrincipal epamEmployeePrincipal = Optional.ofNullable(request.getUserPrincipal())
                .map(o -> (OAuth2Authentication) o)
                .map(u -> (EpamEmployeePrincipal) u.getPrincipal())
                .orElseGet(() -> {
                    log.warn("Unsecured invocation detected");
                    return EpamEmployeePrincipal.builder()
                            .email("John_Doe@epam.com")
                            .userInDistributionList(true)
                            .build();
                });
        Optional<User> userFromDatabase = userService.findUserByEmailIfExist(epamEmployeePrincipal.getEmail());

        if(userFromDatabase.isPresent()){

            if (!userFromDatabase.get().getIsActive()) {
                epamEmployeePrincipal.setUserInDistributionList(false);
            }

        } else {
            epamEmployeePrincipal.setUserInDistributionList(false);
        }

        return epamEmployeePrincipal;
    }
}

