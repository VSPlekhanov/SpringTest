package com.epam.lstrsum.persistence;


import com.epam.lstrsum.model.Subscription;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SubscriptionRepository extends MongoRepository<Subscription, String> {
}
