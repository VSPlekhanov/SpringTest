package com.epam.lstrsum.dto.answer;

import com.epam.lstrsum.dto.question.QuestionBaseDto;
import com.epam.lstrsum.dto.user.UserBaseDto;
import lombok.Getter;

import java.time.Instant;

@Getter
public class AnswerAllFieldsDto extends AnswerBaseDto {
    private String answerId;
    private QuestionBaseDto parentId;

    public AnswerAllFieldsDto(String text, Instant createdAt, UserBaseDto authorId, Integer upVote, String answerId,
                              QuestionBaseDto parentId) {
        super(text, createdAt, authorId, upVote);
        this.answerId = answerId;
        this.parentId = parentId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        AnswerAllFieldsDto that = (AnswerAllFieldsDto) o;

        if (answerId != null ? !answerId.equals(that.answerId) : that.answerId != null) return false;
        return parentId != null ? parentId.equals(that.parentId) : that.parentId == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (answerId != null ? answerId.hashCode() : 0);
        result = 31 * result + (parentId != null ? parentId.hashCode() : 0);
        return result;
    }
}
