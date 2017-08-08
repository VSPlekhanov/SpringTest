package com.epam.lstrsum.converter;

import com.epam.lstrsum.SetUpDataBaseCollections;
import com.epam.lstrsum.dto.answer.AnswerBaseDto;
import com.epam.lstrsum.dto.user.UserBaseDto;
import com.epam.lstrsum.model.Vote;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;

import static com.epam.lstrsum.InstantiateUtil.*;
import static org.assertj.core.api.Assertions.assertThat;

public class VoteDtoMapperTest extends SetUpDataBaseCollections {
    @Autowired
    private VoteDtoMapper voteMapper;

    @Test
    public void modelToAllFieldsDto() throws Exception {
        final Vote vote = someVote();
        final UserBaseDto userBaseDto = someUserBaseDto();
        final AnswerBaseDto answerBaseDto = someAnswerBaseDto();

        assertThat(voteMapper.modelToAllFieldsDto(vote, userBaseDto, answerBaseDto))
                .satisfies(v -> {
                    assertThat(v.getAnswerBaseDto()).isEqualToComparingFieldByFieldRecursively(answerBaseDto);
                    assertThat(v.getUserBaseDto()).isEqualToComparingFieldByFieldRecursively(userBaseDto);
                    assertThat(v.getCreatedAt()).isBeforeOrEqualTo(Instant.now());
                });
    }
}
