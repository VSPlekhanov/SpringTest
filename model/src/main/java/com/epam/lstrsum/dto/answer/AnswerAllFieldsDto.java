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
    private QuestionBaseDto question;

    public AnswerAllFieldsDto(String text,
            Instant createdAt,
            UserBaseDto author,
            int upVote,
            String answerId,
            QuestionBaseDto question,
            Boolean userVoted) {

        super(answerId, text, createdAt, author, upVote, userVoted);
        this.question = question;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        AnswerAllFieldsDto that = (AnswerAllFieldsDto) o;

        return Objects.equals(question, that.question);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), question);
    }
}
