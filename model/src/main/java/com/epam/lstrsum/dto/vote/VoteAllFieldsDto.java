package com.epam.lstrsum.dto.vote;

import com.epam.lstrsum.dto.answer.AnswerBaseDto;
import com.epam.lstrsum.dto.user.UserBaseDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class VoteAllFieldsDto {
    private String voteId;
    private Instant createdAt;
    private boolean isRevoked;
    private AnswerBaseDto answerBaseDto;
    private UserBaseDto userBaseDto;
}
