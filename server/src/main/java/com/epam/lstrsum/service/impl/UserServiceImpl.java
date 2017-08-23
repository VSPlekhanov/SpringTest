package com.epam.lstrsum.service.impl;

import com.epam.lstrsum.aggregators.UserAggregator;
import com.epam.lstrsum.dto.user.telescope.TelescopeDataDto;
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

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserAggregator userAggregator;
    private final MongoTemplate mongoTemplate;
    private final TelescopeService telescopeService;

    private static final BiFunction<List<String>, Set<String>, String> GET_EMAIL_CONTAINS_IN_BOTH_LISTS =
            (emailsFromDto, emailsFromInputSet) -> {
                List<String> helper = new ArrayList<>(emailsFromDto);
                helper.retainAll(emailsFromInputSet);
                return helper.get(0);
            };

    private static final BiFunction<TelescopeDataDto, Set<String>, Map.Entry> GET_MAP_WITH_EMAIL_AND_TELESCOPE_DATA =
            (data, emails) -> new AbstractMap.SimpleEntry<>(
                    GET_EMAIL_CONTAINS_IN_BOTH_LISTS.apply(Arrays.asList(data.getEmail()), emails),
                    data
            );

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public User findUserByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email).orElseThrow(() -> new NoSuchUserException("No such User in user Collection"));
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
        return Optional.ofNullable(userRepository.findOne(userId))
                .orElseThrow(() -> new NoSuchUserException("No such User in user Collection"));
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
    public long addIfNotExistAllWithRole(final List<String> userEmails, List<UserRoleType> roles) {
        Set<String> lowerCaseUsersEmail = userEmails.stream().map(String::toLowerCase).collect(Collectors.toSet());
        List<TelescopeEmployeeEntityDto> telescopeUsersDto = telescopeService.getUsersInfoByEmails(lowerCaseUsersEmail);
        if (telescopeUsersDto.size() < 1) {
            log.warn("No users from emails list size ={} were added", userEmails.size());
            return 0;
        }

        return telescopeUsersDto.stream()
                .map(TelescopeEmployeeEntityDto::getData)
                .filter(data -> !isNullOrEmptyString(data.getFirstName()) && !isNullOrEmptyString(data.getLastName()))
                .map(data -> GET_MAP_WITH_EMAIL_AND_TELESCOPE_DATA.apply(data, lowerCaseUsersEmail))
                .filter(entry -> addIfNotExist(entry, roles))
                .count();
    }

    private boolean addIfNotExist(Map.Entry<String, TelescopeDataDto> entry, List<UserRoleType> roles) {
        final Optional<User> byEmail = userRepository.findByEmailIgnoreCase(entry.getKey());
        if (!byEmail.isPresent()) {
            addNewUserByTelescopeUserData(entry, roles);
            return true;
        }
        return false;
    }

    private void addNewUserByTelescopeUserData(Map.Entry<String, TelescopeDataDto> emailTelescopeDataEntry, List<UserRoleType> userRoles) {
        String userEmail = emailTelescopeDataEntry.getKey();
        TelescopeDataDto userData = emailTelescopeDataEntry.getValue();
        User newUser = userAggregator.userTelescopeInfoDtoToUser(userData, userEmail, userRoles);
        userRepository.save(newUser);
        log.debug("New user with email = {} was added", userEmail);
    }

    private boolean isNullOrEmptyString(String stringForCheck) {
        return isNull(stringForCheck) || stringForCheck.trim().isEmpty();
    }
}

