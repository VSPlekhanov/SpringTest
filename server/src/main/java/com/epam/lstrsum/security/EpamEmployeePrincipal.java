package com.epam.lstrsum.security;

import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.security.Principal;
import java.util.Map;
import java.util.Objects;

@Builder
@Getter
public class EpamEmployeePrincipal implements Principal, Serializable {

    public static final String USER_ID = "upn";
    public static final String UNIQUE_NAME = "unique_name";
    public static final String EMAIL = "email";

    private String userId;
    private String email;
    private String displayName;

    @Override
    public String getName() {
        return displayName;
    }

    public static EpamEmployeePrincipal ofMap(Map<String, Object> map) throws IllegalArgumentException {
        try {
            EpamEmployeePrincipalBuilder principalBuilder = EpamEmployeePrincipal.builder();

            String userId = (String) map.get(USER_ID);
            principalBuilder.userId(Objects.requireNonNull(userId));

            String displayName = (String) map.get(UNIQUE_NAME);
            principalBuilder.displayName(Objects.requireNonNull(displayName));

            String email = (String) map.get(EMAIL);
            principalBuilder.email(Objects.requireNonNull(email));

            return principalBuilder.build();
        } catch (ClassCastException | NullPointerException e) {
            throw new IllegalArgumentException("Wrong map format.", e);
        }
    }

}

