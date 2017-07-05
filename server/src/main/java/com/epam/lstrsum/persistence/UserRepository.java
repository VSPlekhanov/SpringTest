package com.epam.lstrsum.persistence;

import com.epam.lstrsum.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;


public interface UserRepository extends MongoRepository<User, String> {
}
