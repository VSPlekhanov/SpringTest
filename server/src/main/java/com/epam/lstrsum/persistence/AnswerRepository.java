package com.epam.lstrsum.persistence;

import com.epam.lstrsum.model.Answer;
import com.epam.lstrsum.model.Request;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface AnswerRepository extends MongoRepository<Answer, String> {
    List<Answer> findAnswersByParentIdOrderByCreatedAtAsc(Request parentId);
}
