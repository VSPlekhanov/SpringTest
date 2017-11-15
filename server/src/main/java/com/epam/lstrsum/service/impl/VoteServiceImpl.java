package com.epam.lstrsum.service.impl;

import com.epam.lstrsum.exception.BusinessLogicException;
import com.epam.lstrsum.model.Question;
import com.epam.lstrsum.model.User;
import com.epam.lstrsum.model.Vote;
import com.epam.lstrsum.service.AnswerService;
import com.epam.lstrsum.service.VoteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import static java.util.Objects.isNull;

@Service
@RequiredArgsConstructor
@Slf4j
public class VoteServiceImpl implements VoteService {
    private final MongoTemplate mongoTemplate;
    private final AnswerService answerService;

    @Override
    public boolean voteForAnswerByUser(String answerId, String userId) {
        Update addVote = new Update().addToSet("answers.$.votes", new Vote(userId));
        log.debug("User " + userId + " voted answer" + answerId);
        return updateQuestionWithAnswerVote(getQueryForAnswerId(answerId), addVote);
    }

    @Override
    public boolean unvoteForAnswerByUser(String answerId, String userId) {
        Update pullVote = new Update().pull("answers.$.votes", new Vote(userId));
        log.debug("User " + userId + " unvoted answer" + answerId);
        return updateQuestionWithAnswerVote(getQueryForAnswerId(answerId), pullVote);
    }

    @Override
    public boolean voteForAnswerByAllowedSub(String answerId, String userEmail) {
        checkAnswerExistsAndUserHasPermission(answerId, userEmail);
        return voteForAnswerByUser(answerId, userEmail);
    }

    @Override
    public boolean unvoteForAnswerByAllowedSub(String answerId, String userEmail) {
        checkAnswerExistsAndUserHasPermission(answerId, userEmail);
        return unvoteForAnswerByUser(answerId, userEmail);
    }

    private boolean updateQuestionWithAnswerVote(Query query, Update update) {
        return mongoTemplate.updateFirst(query, update, Question.class).isUpdateOfExisting();
    }

    private Query getQueryForAnswerId(String answerId) {
        return new Query(Criteria.where("answers.answerId").is(answerId));
    }

    private void checkAnswerExistsAndUserHasPermission(String answerId, String userEmail) {
        Question question = mongoTemplate.findOne(getQueryForAnswerId(answerId), Question.class);
        if ( isNull(answerService.getAnswerByIdAndQuestionId(answerId, question.getQuestionId())) ||
                isUserAllowedSubOnQuestion(question, userEmail) ) {
            BusinessLogicException e = new BusinessLogicException(
                    "Answer doesn't exist or user with email : '" + userEmail + " ' has no permission to answer id : '" + answerId);
            log.error(e.getMessage());
            throw e;
        }
    }

    private boolean isUserAllowedSubOnQuestion(Question question, String userEmail) {
        return question.getAllowedSubs().stream().map(User::getEmail).filter(e -> e.equals(userEmail)).count() == 1;
    }

}
