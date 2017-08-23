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

    /**
     * Performs a request to telescope api to get users info.
     *
     * @param fullName String name/part name for users elastic search into telescope
     * @param limit    int value with max users amount in response
     * @return json with users info
     */
    TelescopeEmployeeEntityDto[] getUserInfoByFullName(String fullName, int limit);

    /**
     * Create a link to telescope to request user photo.
     *
     * @param uri format example "attachment:///upsa_profilePhoto.4060741400007345041_1.GIF_cba0891d-a69f-47c9-96ib-c61a14e6e33d"
     * @return string with link to telescope api to get user photo
     */
    String getUserPhotoByUri(String uri);
}
