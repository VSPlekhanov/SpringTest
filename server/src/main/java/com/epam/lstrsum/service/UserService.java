package com.epam.lstrsum.service;

import com.epam.lstrsum.aggregators.UserAggregator;
import com.epam.lstrsum.dto.user.UserBaseDto;
import com.epam.lstrsum.exception.NoSuchUserException;
import com.epam.lstrsum.model.User;
import com.epam.lstrsum.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserAggregator userAggregator;
    private final MongoTemplate mongoTemplate;

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
}
