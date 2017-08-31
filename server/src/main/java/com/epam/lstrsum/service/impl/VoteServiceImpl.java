package com.epam.lstrsum.service.impl;

import com.epam.lstrsum.model.Answer;
import com.epam.lstrsum.model.Vote;
import com.epam.lstrsum.service.VoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VoteServiceImpl implements VoteService {
    private final MongoTemplate mongoTemplate;

    @Override
    public boolean voteForAnswerByUser(String answerId, String userId) {
        Query findAnswer = new Query(Criteria.where("answerId").is(answerId));
        Update addVote = new Update().addToSet("votes", new Vote(userId));

        return mongoTemplate.updateFirst(findAnswer, addVote, Answer.class).isUpdateOfExisting();
    }

    @Override
    public boolean unvoteForAnswerByUser(String answerId, String userId) {
        Query findAnswer = new Query(Criteria.where("answerId").is(answerId));
        Update pullVote = new Update().pull("votes", new Vote(userId));

        return mongoTemplate.updateFirst(findAnswer, pullVote, Answer.class).isUpdateOfExisting();

    }
}
