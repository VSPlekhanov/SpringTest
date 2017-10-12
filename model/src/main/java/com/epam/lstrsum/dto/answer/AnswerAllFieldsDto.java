package com.epam.lstrsum.dto.answer;

import com.epam.lstrsum.dto.question.QuestionBaseDto;
import com.epam.lstrsum.dto.user.UserBaseDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
public class AnswerAllFieldsDto extends AnswerBaseDto {
//    private String answerId;
    private QuestionBaseDto questionId;

    public AnswerAllFieldsDto(String text,
            Instant createdAt,
            UserBaseDto authorId,
            int upVote,
            String answerId,
            QuestionBaseDto questionId) {
        super(answerId, text, createdAt, authorId, upVote);
//        this.answerId = answerId;
        this.questionId = questionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        AnswerAllFieldsDto that = (AnswerAllFieldsDto) o;

        if (getAnswerId() != null ? !getAnswerId().equals(that.getAnswerId()) : that.getAnswerId() != null) return false;
        return questionId != null ? questionId.equals(that.questionId) : that.questionId == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (getAnswerId() != null ? getAnswerId().hashCode() : 0);
        result = 31 * result + (questionId != null ? questionId.hashCode() : 0);
        return result;
    }
}
