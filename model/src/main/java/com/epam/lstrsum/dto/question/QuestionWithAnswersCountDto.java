package com.epam.lstrsum.dto.question;

import com.epam.lstrsum.dto.user.UserBaseDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
public class QuestionWithAnswersCountDto extends QuestionBaseDto {
    private Integer answersCount;

    public QuestionWithAnswersCountDto(
            String questionId, String title, String[] tags, Instant createdAt, Instant deadLine,
            UserBaseDto authorId, Integer answersCount
    ) {
        super(questionId, title, tags, createdAt, deadLine, authorId);
        this.answersCount = answersCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        QuestionWithAnswersCountDto that = (QuestionWithAnswersCountDto) o;

        return answersCount.equals(that.answersCount);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + answersCount.hashCode();
        return result;
    }
}
