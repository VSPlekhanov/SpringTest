package com.epam.lstrsum.service;

public interface VoteService {
    boolean voteForAnswerByUser(String answerId, String userEmail);

    boolean unvoteForAnswerByUser(String answerId, String userEmail);

    boolean voteForAnswerByAllowedSub(String answerId, String userEmail);

    boolean unvoteForAnswerByAllowedSub(String answerId, String userEmail);
}
