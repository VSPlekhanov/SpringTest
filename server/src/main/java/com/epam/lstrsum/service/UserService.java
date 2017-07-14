package com.epam.lstrsum.service;

import com.epam.lstrsum.model.User;
import com.epam.lstrsum.persistence.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).get();
    }

    public User getUserById(String userId) {
        return userRepository.findOne(userId);
    }
}
