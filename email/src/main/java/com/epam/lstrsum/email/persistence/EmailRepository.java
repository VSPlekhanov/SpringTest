package com.epam.lstrsum.email.persistence;

import com.epam.lstrsum.model.Email;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface EmailRepository extends MongoRepository<Email, String> {
    
}
