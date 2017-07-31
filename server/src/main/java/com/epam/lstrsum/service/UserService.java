package com.epam.lstrsum.service;

import com.epam.lstrsum.exception.NoSuchUserException;
import com.epam.lstrsum.model.User;
import com.epam.lstrsum.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new NoSuchUserException("No such User in user Collection"));
    }

    public User getUserById(String userId) {
        return userRepository.findOne(userId);
    }
}
