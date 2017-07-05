package com.epam.lstrsum.persistence;


import com.epam.lstrsum.model.Answer;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AnswerRepository extends MongoRepository<Answer, String> {
}
