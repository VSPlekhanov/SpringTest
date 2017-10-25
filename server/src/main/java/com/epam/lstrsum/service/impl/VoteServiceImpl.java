package com.epam.lstrsum.service.impl;

import com.epam.lstrsum.model.Answer;
import com.epam.lstrsum.model.Question;
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
        Query findAnswer = new Query(Criteria.where("answers.answerId").is(answerId));
        Update addVote = new Update().addToSet("answers.$.votes", new Vote(userId));

        return mongoTemplate.updateFirst(findAnswer, addVote, Question.class).isUpdateOfExisting();
    }

    @Override
    public boolean unvoteForAnswerByUser(String answerId, String userId) {
        Query findAnswer = new Query(Criteria.where("answers.answerId").is(answerId));
        Update pullVote = new Update().pull("answers.$.votes", new Vote(userId));

        return mongoTemplate.updateFirst(findAnswer, pullVote, Question.class).isUpdateOfExisting();

    }
}
