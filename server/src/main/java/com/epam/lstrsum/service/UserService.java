package com.epam.lstrsum.service;

import com.epam.lstrsum.aggregators.UserAggregator;
import com.epam.lstrsum.dto.user.UserBaseDto;
import com.epam.lstrsum.dto.user.telescope.TelescopeDataDto;
import com.epam.lstrsum.dto.user.telescope.TelescopeEmployeeEntityDto;
import com.epam.lstrsum.enums.UserRoleType;
import com.epam.lstrsum.exception.NoSuchUserException;
import com.epam.lstrsum.model.User;
import com.epam.lstrsum.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.isNull;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserAggregator userAggregator;
    private final MongoTemplate mongoTemplate;
    private final TelescopeService telescopeService;

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new NoSuchUserException("No such User in user Collection"));
    }

    public long setActiveForAllAs(Collection<? super String> emails, boolean active) {
        final Criteria inEmails = Criteria.where("email").in(emails);
        return mongoTemplate.updateMulti(new Query(inEmails), Update.update("isActive", active), User.class).getN();
    }

    public List<User> findAllWithRole(final String role) {
        final Criteria roles = Criteria.where("roles").elemMatch(new Criteria().in(role));
        return mongoTemplate.find(new Query(roles), User.class);
    }

    public User findUserById(String userId) {
        return Optional.ofNullable(userRepository.findOne(userId)).orElseThrow(() -> new NoSuchUserException("No such User in user Collection"));
    }

    public UserBaseDto modelToBaseDto(User authorId) {
        return userAggregator.modelToBaseDto(authorId);
    }

    public long addIfNotExistAllWithRole(final List<String> userEmails, String[] roles) {
        // TODO: 8/9/2017 RETRIEVE INFORMATION ABOUT ALL USERS FROM TELESCOPE
        return userEmails.stream()
                .filter(email -> addIfNotExist(email, roles))
                .count();
    }

    private boolean addIfNotExist(final String email, String[] roles) {
        final Optional<User> byEmail = userRepository.findByEmail(email);
        if (!byEmail.isPresent()) {
            userRepository.save(User.builder()
                    .email(email)
                    .roles(roles)
                    .createdAt(Instant.now())
                    .isActive(true)
                    .build());
            return true;
        }
        return false;
    }

    public TelescopeEmployeeEntityDto[] getUserInfoByFullName(String fullName, Integer limit) {
        return telescopeService.getUserInfoByFullName(fullName, limit);
    }

    public String getUserPhotoByUri(String uri) {
        return telescopeService.getUserPhotoByUri(uri);
    }

    /**
     * Add a new user to a database only if a user information was received from telescope api.
     *
     * @param email     user email must ended on "@epam.com"
     * @param userRoles {@link UserRoleType} scope with user security roles
     */
    public void addNewUserByEmail(String email, List<UserRoleType> userRoles) {

        TelescopeEmployeeEntityDto[] telescopeUserDto = telescopeService.getUserInfoByEmail(email);
        if (!isUserInfoWereReceivedFromTelescope(telescopeUserDto, email)) {
            return;
        }
        User newUser = userAggregator.userTelescopeInfoDtoToUser(telescopeUserDto[0].getData(), email, userRoles);
        userRepository.save(newUser);

        log.debug("New user with email = {} was added", email);
    }

    private boolean isUserInfoWereReceivedFromTelescope(TelescopeEmployeeEntityDto[] telescopeEmployeeEntityDto, String email) {
        if (isNull(telescopeEmployeeEntityDto) || telescopeEmployeeEntityDto.length == 0 || isNull(telescopeEmployeeEntityDto[0].getData())) {
            log.warn("New user with email = {} wasn't added because of no data was received from telescope api", email);
            return false;
        }
        TelescopeDataDto userDto = telescopeEmployeeEntityDto[0].getData();
        if (isNullOrEmptyString(userDto.getFirstName()) || isNullOrEmptyString(userDto.getLastName())) {
            log.warn("New user with email = {} wasn't added because of no firstName and/or lastName were received from telescope api", email);
            return false;
        }
        return true;
    }

    private boolean isNullOrEmptyString(String stringForCheck) {
        return isNull(stringForCheck) || stringForCheck.trim().isEmpty();
    }
}
