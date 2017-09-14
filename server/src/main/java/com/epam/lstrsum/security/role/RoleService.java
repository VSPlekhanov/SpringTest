package com.epam.lstrsum.security.role;

import com.epam.lstrsum.model.User;
import com.epam.lstrsum.security.EpamEmployeePrincipal;

import java.util.Map;

/**
 * Implement this interface to build custom RoleServices.
 * <p>
 * RoleService provides the information about Principal roles, to
 * define the authorities after SSO authorization.
 * <p>
 * Also this service provide mapping of requests and required roles.
 */
public interface RoleService {

    /**
     * Get roles of specified principal.
     * <p>
     * If principal have no roles this method should return
     * one role like "ROLE_NOT_ALLOWED_USER".
     * <p>
     * Kepp in mind, if this method is calling, it means that
     * principal already have passed the authentication system.
     *
     * @param principal - Authinticated principal
     * @return - array or roles or not allowed user role
     * @throws NullPointerException - if principal is null
     */
    String[] getPrincipalRoles(EpamEmployeePrincipal principal);

    /**
     * Get roles mappping.
     * <p>
     * For each request pattern should return not
     * empty roles list.
     *
     * @return - mapping for requests and roles
     */
    Map<String, String[]> getRolesRequestsMapping();

    /**
     * Should return name of "ROLE_NOT_ALLOWED_USER" role.
     *
     * @return name of not allowed user role.
     */
    String getNotAllowedPrincipalRole();

    String[] getPrincipalRoles(User user);
}

