package com.epam.lstrsum.service.impl;

import com.epam.lstrsum.aggregators.UserAggregator;
import com.epam.lstrsum.dto.user.UserBaseDto;
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
import lombok.val;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

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
    //List<String> -> Set<String> -> String
    private static final BiFunction<List<String>, Set<String>, String> GET_EMAIL_CONTAINS_IN_BOTH_LISTS =
            (emailsFromDto, emailsFromInputSet) -> emailsFromDto.stream()
                    .filter(emailsFromInputSet::contains)
                    .findFirst()
                    .orElseThrow(RuntimeException::new);
    //TelescopeDataDto -> Set<String> -> Pair<String, TelescopeDataDto>
    private static final BiFunction<TelescopeDataDto, Set<String>, Pair<String, TelescopeDataDto>>
            GET_PAIR_FROM_EMAIL_TO_TELESCOPE_DATA = (data, emails) ->
            ImmutablePair.of(
                    GET_EMAIL_CONTAINS_IN_BOTH_LISTS.apply(data.getEmail(), emails),
                    data
            );
    private final UserRepository userRepository;
    private final UserAggregator userAggregator;
    private final MongoTemplate mongoTemplate;
    private final TelescopeService telescopeService;

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public List<User> findAllActive() {
        return userRepository.findAllByIsActive(true);
    }

    @Override
    public User findUserByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email).orElseThrow(() -> new NoSuchUserException("No such User in user Collection"));
    }

    @Override
    public long setActiveForAllAs(Collection<? super String> emails, boolean active) {
        val inEmails = Criteria.where("email").in(emails);
        return mongoTemplate.updateMulti(new Query(inEmails), Update.update("isActive", active), User.class).getN();
    }

    @Override
    public List<User> findAllWithRole(final UserRoleType role) {
        val roles = Criteria.where("roles").elemMatch(new Criteria().in(role));
        return mongoTemplate.find(new Query(roles), User.class);
    }

    @Override
    public boolean existsActiveUserWithRoleAndEmail(UserRoleType role, String email) {
        val query = Criteria.where("roles").elemMatch(new Criteria().in(role))
                .andOperator(Criteria.where("isActive").is(true)
                .andOperator(Criteria.where("email").regex(email, "i")));
        return mongoTemplate.exists(new Query(query), User.class);
    }

    @Override
    public User findUserById(String userId) {
        return Optional.ofNullable(userRepository.findOne(userId))
                .orElseThrow(() -> new NoSuchUserException("No such User in user Collection"));
    }

    @Override
    public long addIfNotExistAllWithRole(final List<String> userEmails, List<UserRoleType> roles) {
        if (isNull(userEmails) || userEmails.isEmpty()) {
            return 0;
        }

        val lowerCaseUsersEmail = userEmails.stream().map(String::toLowerCase).collect(Collectors.toSet());
        val telescopeUsersDto = telescopeService.getUsersInfoByEmails(lowerCaseUsersEmail);
        if (telescopeUsersDto.size() < 1) {
            log.warn("No users from emails list size ={} were added", userEmails.size());
            return 0;
        }

        return telescopeUsersDto.stream()
                .map(TelescopeEmployeeEntityDto::getData)
                .filter(data -> !isNullOrEmptyString(data.getFirstName()) && !isNullOrEmptyString(data.getLastName()))
                .map(data -> GET_PAIR_FROM_EMAIL_TO_TELESCOPE_DATA.apply(data, lowerCaseUsersEmail))
                .filter(entry -> addIfNotExist(entry, roles))
                .count();
    }

    @Override
    public List<UserBaseDto> findAllUserBaseDtos() {
        return userAggregator.allowedSubsToListOfUserBaseDtos(findAll());
    }

    private boolean addIfNotExist(Map.Entry<String, TelescopeDataDto> entry, List<UserRoleType> roles) {
        val byEmail = userRepository.findByEmailIgnoreCase(entry.getKey());
        if (!byEmail.isPresent()) {
            addNewUserByTelescopeUserData(entry, roles);
            return true;
        }
        return false;
    }

    private void addNewUserByTelescopeUserData(Map.Entry<String, TelescopeDataDto> emailTelescopeDataEntry, List<UserRoleType> userRoles) {
        val userEmail = emailTelescopeDataEntry.getKey();
        val userData = emailTelescopeDataEntry.getValue();
        val newUser = userAggregator.userTelescopeInfoDtoToUser(userData, userEmail, userRoles);
        userRepository.save(newUser);
        log.debug("New user with email = {} was added", userEmail);
    }

    private boolean isNullOrEmptyString(String stringForCheck) {
        return isNull(stringForCheck) || stringForCheck.trim().isEmpty();
    }
}

