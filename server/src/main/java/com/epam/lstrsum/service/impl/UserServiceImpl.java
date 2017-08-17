package com.epam.lstrsum.service.impl;

import com.epam.lstrsum.aggregators.UserAggregator;
import com.epam.lstrsum.dto.user.telescope.TelescopeEmployeeEntityDto;
import com.epam.lstrsum.enums.UserRoleType;
import com.epam.lstrsum.exception.NoSuchUserException;
import com.epam.lstrsum.model.User;
import com.epam.lstrsum.persistence.UserRepository;
import com.epam.lstrsum.service.TelescopeService;
import com.epam.lstrsum.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserAggregator userAggregator;
    private final MongoTemplate mongoTemplate;
    private final TelescopeService telescopeService;

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new NoSuchUserException("No such User in user Collection"));
    }

    @Override
    public long setActiveForAllAs(Collection<? super String> emails, boolean active) {
        final Criteria inEmails = Criteria.where("email").in(emails);
        return mongoTemplate.updateMulti(new Query(inEmails), Update.update("isActive", active), User.class).getN();
    }

    @Override
    public List<User> findAllWithRole(final UserRoleType role) {
        final Criteria roles = Criteria.where("roles").elemMatch(new Criteria().in(role));
        return mongoTemplate.find(new Query(roles), User.class);
    }

    @Override
    public User findUserById(String userId) {
        return Optional.ofNullable(userRepository.findOne(userId)).orElseThrow(() -> new NoSuchUserException("No such User in user Collection"));
    }

    @Override
    public long addIfNotExistAllWithRole(final List<String> userEmails, List<UserRoleType> roles) {
        // TODO: 8/9/2017 RETRIEVE INFORMATION ABOUT ALL USERS FROM TELESCOPE
        return userEmails.stream()
                .filter(email -> addIfNotExist(email, roles))
                .count();
    }

    @Override
    public TelescopeEmployeeEntityDto[] getUserInfoByFullName(String fullName, int limit) {
        return telescopeService.getUsersInfoByFullName(fullName, limit);
    }

    @Override
    public String getUserPhotoByUri(String uri) {
        return telescopeService.getUserPhotoByUri(uri);
    }

    @Override
    public User addNewUserByEmail(String email, List<UserRoleType> userRoles) {
        final User newUser = userAggregator.userTelescopeInfoDtoToUser(
                telescopeService.getUserInfoByEmail(email).getData(),
                email, userRoles
        );

        userRepository.save(newUser);
        log.debug("Add user with email {}", email);
        return newUser;
    }

    private boolean addIfNotExist(final String email, List<UserRoleType> roles) {
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
}
