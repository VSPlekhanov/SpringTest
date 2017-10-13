package com.epam.lstrsum.dto.answer;

import com.epam.lstrsum.dto.user.UserBaseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@AllArgsConstructor
@Getter
@Setter
@Builder
@NoArgsConstructor
public class AnswerBaseDto {
    private String answerId;
    private String text;
    private Instant createdAt;
    private UserBaseDto author;
    private Integer upVote;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AnswerBaseDto that = (AnswerBaseDto) o;

        if (answerId != null ? !answerId.equals(that.answerId) : that.answerId != null) return false;
        if (text != null ? !text.equals(that.text) : that.text != null) return false;
        if (createdAt != null ? !createdAt.equals(that.createdAt) : that.createdAt != null) return false;
        if (author != null ? !author.equals(that.author) : that.author != null) return false;
        return upVote != null ? upVote.equals(that.upVote) : that.upVote == null;
    }

    @Override
    public int hashCode() {
        int result = text != null ? text.hashCode() : 0;
        result = 31 * result + (answerId != null ? answerId.hashCode() : 0);
        result = 31 * result + (createdAt != null ? createdAt.hashCode() : 0);
        result = 31 * result + (author != null ? author.hashCode() : 0);
        result = 31 * result + (upVote != null ? upVote.hashCode() : 0);
        return result;
    }
}
