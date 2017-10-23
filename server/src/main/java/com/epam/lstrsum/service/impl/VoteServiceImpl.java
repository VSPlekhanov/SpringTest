package com.epam.lstrsum.service.impl;

import com.epam.lstrsum.exception.BusinessLogicException;
import com.epam.lstrsum.model.Answer;
import com.epam.lstrsum.model.User;
import com.epam.lstrsum.model.Vote;
import com.epam.lstrsum.service.AnswerService;
import com.epam.lstrsum.service.VoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import static java.util.Objects.isNull;

@Service
@RequiredArgsConstructor
public class VoteServiceImpl implements VoteService {
    private final MongoTemplate mongoTemplate;
    private final AnswerService answerService;

    @Override
    public boolean voteForAnswerByUser(String answerId, String userId) {
        Update addVote = new Update().addToSet("votes", new Vote(userId));
        return updateAnswerWithVote(getQueryForUpdate(answerId), addVote);
    }

    @Override
    public boolean unvoteForAnswerByUser(String answerId, String userId) {
        Update pullVote = new Update().pull("votes", new Vote(userId));
        return updateAnswerWithVote(getQueryForUpdate(answerId), pullVote);
    }

    @Override
    public boolean voteForAnswerByAllowedSub(String answerId, String userEmail) {
        checkAnswerExistAndUserHasPermission(answerService.getAnswerById(answerId), userEmail);
        return voteForAnswerByUser(answerId, userEmail);
    }

    @Override
    public boolean unvoteForAnswerByAllowedSub(String answerId, String userEmail) {
        checkAnswerExistAndUserHasPermission(answerService.getAnswerById(answerId), userEmail);
        return unvoteForAnswerByUser(answerId, userEmail);
    }

    private boolean updateAnswerWithVote(Query query, Update update) {
        return mongoTemplate.updateFirst(query, update, Answer.class).isUpdateOfExisting();
    }

    private Query getQueryForUpdate(String answerId) {
        return new Query(Criteria.where("answerId").is(answerId));
    }

    private void checkAnswerExistAndUserHasPermission(Answer answer, String userEmail) {
        if (isNull(answer) || isUserAllowedSubOnQuestion(answer, userEmail)) {
            throw new BusinessLogicException(
                    "Answer isn't exist or user with email : '" + userEmail + " ' has no permission to answer id : '" +
                            answer.getAnswerId());
        }
    }

    private boolean isUserAllowedSubOnQuestion(Answer answer, String userEmail) {
        return answer.getQuestionId().getAllowedSubs().stream().map(User::getEmail).filter(e -> e.equals(userEmail)).count() == 1;
    }
}
