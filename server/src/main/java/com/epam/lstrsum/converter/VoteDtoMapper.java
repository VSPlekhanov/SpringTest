package com.epam.lstrsum.converter;

import com.epam.lstrsum.dto.answer.AnswerBaseDto;
import com.epam.lstrsum.dto.user.UserBaseDto;
import com.epam.lstrsum.dto.vote.VoteAllFieldsDto;
import com.epam.lstrsum.model.Vote;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface VoteDtoMapper {
    @Mappings({
            @Mapping(target = "createdAt", source = "vote.createdAt")
    })
    VoteAllFieldsDto modelToAllFieldsDto(Vote vote, UserBaseDto userBaseDto, AnswerBaseDto answerBaseDto);
}