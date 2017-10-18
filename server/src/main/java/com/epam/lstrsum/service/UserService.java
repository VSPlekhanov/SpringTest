package com.epam.lstrsum.service;

import com.epam.lstrsum.dto.user.UserBaseDto;
import com.epam.lstrsum.enums.UserRoleType;
import com.epam.lstrsum.model.User;

import java.util.Collection;
import java.util.List;

public interface UserService {

    List<User> findAll();

    List<User> findAllActive();

    User findUserByEmail(String email);

    long setActiveForAllAs(Collection<? super String> emails, boolean active);

    List<User> findAllWithRole(final UserRoleType role);

    User findUserById(String userId);

    long addIfNotExistAllWithRole(final List<String> userEmails, UserRoleType role);

    List<UserBaseDto> findAllUserBaseDtos();

    boolean existsActiveUserWithRoleAndEmail(final UserRoleType role, String email);
}
