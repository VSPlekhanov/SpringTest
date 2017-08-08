package com.epam.lstrsum.service;

import com.epam.lstrsum.aggregators.UserAggregator;
import com.epam.lstrsum.dto.user.UserBaseDto;
import com.epam.lstrsum.exception.NoSuchUserException;
import com.epam.lstrsum.model.User;
import com.epam.lstrsum.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserAggregator userAggregator;

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new NoSuchUserException("No such User in user Collection"));
    }

    public User getUserById(String userId) {
        return Optional.ofNullable(userRepository.findOne(userId)).orElseThrow(() -> new NoSuchUserException("No such User in user Collection"));
    }

    public UserBaseDto modelToBaseDto(User authorId) {
        return userAggregator.modelToBaseDto(authorId);
    }
}
