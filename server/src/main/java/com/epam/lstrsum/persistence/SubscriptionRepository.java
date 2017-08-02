package com.epam.lstrsum.persistence;


import com.epam.lstrsum.model.Subscription;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface SubscriptionRepository extends MongoRepository<Subscription, String> {
    @Query(fields = "{userId:1}")
    List<Subscription> findAllByQuestionIdsContains(String questionId);
}
