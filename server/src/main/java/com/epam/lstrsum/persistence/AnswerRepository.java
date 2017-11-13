package com.epam.lstrsum.persistence;

import com.epam.lstrsum.model.Answer;
import com.epam.lstrsum.model.Question;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface AnswerRepository extends MongoRepository<Answer, String> {

}
