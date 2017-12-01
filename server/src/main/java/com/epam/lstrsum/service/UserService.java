package com.epam.lstrsum.service;

import com.epam.lstrsum.dto.user.UserBaseDto;
import com.epam.lstrsum.enums.UserRoleType;
import com.epam.lstrsum.model.User;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserService {

    List<User> findAll();

    List<User> findAllActive();

    User findUserByEmailOrThrowException(String email);

    Optional<User> findUserByEmailIfExist(String email);

    long setActiveForAllAs(Collection<? super String> emails, boolean active);

    List<User> findAllWithRole(final UserRoleType role);

    User findUserById(String userId);

    long addIfNotExistAllWithRole(final List<String> userEmails, UserRoleType role);

    List<UserBaseDto> findAllUserBaseDtos();

    boolean existsActiveUserWithRoleAndEmail(final UserRoleType role, String email);
}
