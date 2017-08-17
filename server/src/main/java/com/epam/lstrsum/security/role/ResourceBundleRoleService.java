package com.epam.lstrsum.security.role;

import com.epam.lstrsum.security.EpamEmployeePrincipal;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * This implementation of RoleService uses ResourceBundle for
 * getting roles settings.
 * <p>
 * User email is used as principal identifier.
 * <p>
 * Properties file example:
 * #Users
 * user@domain.com=ROLE_USER
 * admin@domain.com=ROLE_USER,ROLE_ADMIN
 * <p>
 * #Mapping
 * allowFor(/api/**)=ROLE_USER,ROLE_ADMIN
 * allowFor(/admin/**)=ROLE_ADMIN
 * <p>
 * #NotAllowedRole
 * not.allowed.role.name=NOT_ALLOWED_USER
 */

public class ResourceBundleRoleService implements RoleService {
    private static final String NOT_ALLOWED_USER_PROPERTY_NAME = "not.allowed.role.name";
    private final ResourceBundle bundle;
    private String notAllowedRole = "NOT_ALLOWED_USER";

    /**
     * New ResourceBundleRoleService
     *
     * @param bundle - valid ResourceBundle
     * @throws NullPointerException if bundle is null
     */
    public ResourceBundleRoleService(ResourceBundle bundle) {
        this.bundle = Objects.requireNonNull(bundle);
    }

    @Override
    public String[] getPrincipalRoles(EpamEmployeePrincipal principal) {
        String email = principal.getEmail();

        if (!bundle.containsKey(email))
            return new String[]{notAllowedRole};

        String[] strings = getMappedRoles(email);
        return Arrays.copyOf(strings, strings.length);
    }

    @Override
    public Map<String, String[]> getRolesRequestsMapping() {
        Map<String, String[]> result = new HashMap<>();

        Enumeration<String> keys = bundle.getKeys();

        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            if (key.contains("allowFor")) {
                String pattern = key.substring(key.indexOf('(') + 1, key.indexOf(')'));
                result.put(pattern, getMappedRoles(key));
            }
        }

        return result;
    }

    @Override
    public String getNotAllowedPrincipalRole() {
        if (bundle.containsKey(NOT_ALLOWED_USER_PROPERTY_NAME)) {
            notAllowedRole = bundle.getString(NOT_ALLOWED_USER_PROPERTY_NAME);
        }
        return notAllowedRole;
    }

    private String[] getMappedRoles(String key) {
        return Arrays.stream(bundle.getString(key).split(","))
                .map(String::trim)
                .toArray(String[]::new);
    }

}
