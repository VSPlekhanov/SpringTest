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
    private QuestionBaseDto question;

    public AnswerAllFieldsDto(String text,
            Instant createdAt,
            UserBaseDto author,
            int upVote,
            String answerId,
            QuestionBaseDto question) {
        super(answerId, text, createdAt, author, upVote);
//        this.answerId = answerId;
        this.question = question;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        AnswerAllFieldsDto that = (AnswerAllFieldsDto) o;

        if (getAnswerId() != null ? !getAnswerId().equals(that.getAnswerId()) : that.getAnswerId() != null) return false;
        return question != null ? question.equals(that.question) : that.question == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (getAnswerId() != null ? getAnswerId().hashCode() : 0);
        result = 31 * result + (question != null ? question.hashCode() : 0);
        return result;
    }
}
