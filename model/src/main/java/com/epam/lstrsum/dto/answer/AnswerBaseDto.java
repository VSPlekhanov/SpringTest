package com.epam.lstrsum.dto.answer;

import com.epam.lstrsum.dto.user.UserBaseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.Objects;

@AllArgsConstructor
@Getter
@Setter
@Builder
@NoArgsConstructor
public class AnswerBaseDto {
    private String answerId;
    private String text;
    private Instant createdAt;
    private UserBaseDto authorId;
    private Integer upVote;
    private Boolean userVoted;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AnswerBaseDto that = (AnswerBaseDto) o;
        return Objects.equals(answerId, that.answerId) &&
                Objects.equals(text, that.text) &&
                Objects.equals(createdAt, that.createdAt) &&
                Objects.equals(authorId, that.authorId) &&
                Objects.equals(upVote, that.upVote) &&
                Objects.equals(userVoted, that.userVoted);
    }

    @Override
    public int hashCode() {
        return Objects.hash(answerId, text, createdAt, authorId, upVote, userVoted);
    }
}
