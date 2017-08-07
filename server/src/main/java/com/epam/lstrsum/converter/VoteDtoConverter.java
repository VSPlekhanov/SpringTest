package com.epam.lstrsum.converter;

import com.epam.lstrsum.dto.vote.VoteAllFieldsDto;
import com.epam.lstrsum.model.Vote;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class VoteDtoConverter implements AllFieldModelDtoConverter<Vote, VoteAllFieldsDto> {

    @Autowired
    private UserDtoConverter userConverter;

    @Autowired
    private AnswerDtoConverter answerConverter;


    @Override
    public VoteAllFieldsDto modelToAllFieldsDto(Vote vote) {
        return new VoteAllFieldsDto(vote.getVoteId(),
                vote.getCreatedAt(),
                vote.getIsRevoked(),
                answerConverter.modelToBaseDto(vote.getAnswerId()),
                userConverter.modelToBaseDto(vote.getUserId()));
    }
}
