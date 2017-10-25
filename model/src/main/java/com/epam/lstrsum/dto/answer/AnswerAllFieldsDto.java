package com.epam.lstrsum.dto.answer;

import com.epam.lstrsum.dto.question.QuestionBaseDto;
import com.epam.lstrsum.dto.user.UserBaseDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
public class AnswerAllFieldsDto extends AnswerBaseDto {
    private String answerId;
    private QuestionBaseDto questionId;

    public AnswerAllFieldsDto(String text,
            Instant createdAt,
            UserBaseDto authorId,
            int upVote,
            String answerId,
            QuestionBaseDto questionId,
            Boolean userVoted) {
        super(answerId, text, createdAt, authorId, upVote, userVoted);
        this.answerId = answerId;
        this.questionId = questionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        AnswerAllFieldsDto that = (AnswerAllFieldsDto) o;
        return Objects.equals(answerId, that.answerId) &&
                Objects.equals(questionId, that.questionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), answerId, questionId);
    }
}
