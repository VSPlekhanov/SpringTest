package com.epam.lstrsum.security;

import lombok.Data;

import java.io.Serializable;
import java.security.Principal;
import java.util.Map;
import java.util.Objects;

@Data
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
            EpamEmployeePrincipal empl = new EpamEmployeePrincipal();
            String userId = (String) map.get(USER_ID);
            empl.setUserId(Objects.requireNonNull(userId));

            String displayName = (String) map.get(UNIQUE_NAME);
            empl.setDisplayName(Objects.requireNonNull(displayName));

            String email = (String) map.get(EMAIL);
            empl.setEmail(Objects.requireNonNull(email));
            return empl;
        } catch (ClassCastException | NullPointerException e) {
            throw new IllegalArgumentException("Wrong map format.", e);
        }
    }

}

