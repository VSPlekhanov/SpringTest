package com.epam.lstrsum.service;

public interface VoteService {
    boolean voteForAnswerByUser(String answerId, String userId);

    boolean unvoteForAnswerByUser(String answerId, String userId);

    boolean voteForAnswerByAllowedSub(String answerId, String email);

    boolean unvoteForAnswerByAllowedSub(String answerId, String email);
}
