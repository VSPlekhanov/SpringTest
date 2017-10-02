package com.epam.lstrsum.security;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.security.Principal;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.nonNull;

@Builder
@Getter
@Slf4j
public class EpamEmployeePrincipal implements Principal, Serializable {

    public static final String USER_ID = "upn";
    public static final String UNIQUE_NAME = "unique_name";
    public static final String EMAIL = "email";
    public static final String DISTRIBUTION_LIST_USER = "dl_user";

    private String userId;
    private String email;
    private String displayName;
    private boolean userInDistributionList;

    public static EpamEmployeePrincipal ofMap(Map<String, Object> map) throws IllegalArgumentException {

        final String userId = validateAndGetByKey(map, USER_ID);
        final String displayName = validateAndGetByKey(map, UNIQUE_NAME);
        final String email = validateAndGetByKey(map, EMAIL);

        final Boolean isDistributionListUser = Optional.ofNullable(map.get(DISTRIBUTION_LIST_USER))
                .map(value -> {
                    if (nonNull(value) && value instanceof Boolean) return (Boolean) value;

                    log.error("Wrong format with key = {}", DISTRIBUTION_LIST_USER);
                    throw new IllegalArgumentException("Wrong map format by key: " + DISTRIBUTION_LIST_USER);
                })
                .orElse(false);

        return EpamEmployeePrincipal.builder()
                .userId(userId)
                .displayName(displayName)
                .email(email)
                .userInDistributionList(isDistributionListUser)
                .build();
    }

    private static String validateAndGetByKey(Map<String, Object> map, String key) throws IllegalArgumentException {
        Object value = map.get(key);
        if (nonNull(value) && value instanceof String) return (String) value;

        log.error("Wrong format with key = {}", key);
        throw new IllegalArgumentException("Wrong map format by key: " + key);
    }

    @Override
    public String getName() {
        return displayName;
    }

}

