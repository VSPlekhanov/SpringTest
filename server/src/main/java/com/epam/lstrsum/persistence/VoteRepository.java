package com.epam.lstrsum.persistence;

import com.epam.lstrsum.model.User;
import com.epam.lstrsum.model.Vote;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface VoteRepository extends MongoRepository<Vote, String> {

    Optional<Vote> findVoteByUserIdAndAnswerId(User user, String answerId);

    @Query("{'answerId' :{'$ref' : 'answer' , '$id' : ?0}}")
    List<Vote> findAllByAnswerId(String answerId);
}
