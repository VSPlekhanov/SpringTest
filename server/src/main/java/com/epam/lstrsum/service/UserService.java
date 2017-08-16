package com.epam.lstrsum.service;

import com.epam.lstrsum.dto.user.telescope.TelescopeEmployeeEntityDto;
import com.epam.lstrsum.enums.UserRoleType;
import com.epam.lstrsum.model.User;

import java.util.Collection;
import java.util.List;

public interface UserService {

    List<User> findAll();

    User findUserByEmail(String email);

    long setActiveForAllAs(Collection<? super String> emails, boolean active);

    List<User> findAllWithRole(final UserRoleType role);

    User findUserById(String userId);

    long addIfNotExistAllWithRole(final List<String> userEmails, List<UserRoleType> roles);

    TelescopeEmployeeEntityDto[] getUserInfoByFullName(String fullName, Integer limit);

    String getUserPhotoByUri(String uri);

    /**
     * Add a new user to a database only if a user information was received from telescope api.
     *
     * @param email     user email must ended on "@epam.com"
     * @param userRoles {@link UserRoleType} scope with user security roles
     */
    User addNewUserByEmail(String email, List<UserRoleType> userRoles);
}
