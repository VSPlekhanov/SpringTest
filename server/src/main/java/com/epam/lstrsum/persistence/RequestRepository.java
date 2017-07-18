package com.epam.lstrsum.persistence;


import com.epam.lstrsum.model.Request;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RequestRepository extends MongoRepository<Request, String> {
}
