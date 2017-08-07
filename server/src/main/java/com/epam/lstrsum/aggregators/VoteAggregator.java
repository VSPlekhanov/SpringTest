package com.epam.lstrsum.aggregators;

import com.epam.lstrsum.converter.AnswerDtoMapper;
import com.epam.lstrsum.converter.UserDtoMapper;
import com.epam.lstrsum.converter.VoteDtoMapper;
import com.epam.lstrsum.converter.contract.AllFieldModelDtoConverter;
import com.epam.lstrsum.dto.user.UserBaseDto;
import com.epam.lstrsum.dto.vote.VoteAllFieldsDto;
import com.epam.lstrsum.model.Vote;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VoteAggregator implements AllFieldModelDtoConverter<Vote, VoteAllFieldsDto> {
    private final UserDtoMapper userMapper;
    private final AnswerDtoMapper answerMapper;
    private final VoteDtoMapper voteMapper;

    @Override
    public VoteAllFieldsDto modelToAllFieldsDto(Vote vote) {
        final UserBaseDto userBaseDto = userMapper.modelToBaseDto(vote.getUserId());

        return voteMapper.modelToAllFieldsDto(
                vote,
                userBaseDto,
                answerMapper.modelToBaseDto(vote.getAnswerId(), userBaseDto)
        );
    }
}
