package com.epam.lstrsum.service;

import com.epam.lstrsum.dto.vote.VoteAllFieldsDto;
import com.epam.lstrsum.model.User;
import com.epam.lstrsum.model.Vote;

import java.util.List;
import java.util.Optional;

public interface VoteService {

    Optional<Vote> getVoteByUserAndAnswerId(User user, String answerId);

    VoteAllFieldsDto addVoteToAnswer(String email, String answerId);

    List<VoteAllFieldsDto> findAllVotesForAnswer(String answerId);

    void deleteVoteToAnswer(String email, String answerId);
}
