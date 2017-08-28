package com.epam.lstrsum.persistence;

import com.epam.lstrsum.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;


public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByEmailIgnoreCase(String email);

    List<User> findAllByIsActive(boolean isActive);
}
