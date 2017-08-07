package com.epam.lstrsum.dto.question;


import com.epam.lstrsum.dto.user.UserBaseDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class QuestionAllFieldsDto extends QuestionBaseDto {
    private List<UserBaseDto> allowedSubs;
    private String text;

    public QuestionAllFieldsDto(String questionId, String title, String[] tags, Instant createdAt, Instant deadLine,
                                UserBaseDto author, Integer upVote, List<UserBaseDto> allowedSubs, String text) {
        super(questionId, title, tags, createdAt, deadLine, author, upVote);
        this.allowedSubs = allowedSubs;
        this.text = text;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        QuestionAllFieldsDto that = (QuestionAllFieldsDto) o;

        if (allowedSubs != null ? !allowedSubs.equals(that.allowedSubs) : that.allowedSubs != null) return false;
        return text != null ? text.equals(that.text) : that.text == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (allowedSubs != null ? allowedSubs.hashCode() : 0);
        result = 31 * result + (text != null ? text.hashCode() : 0);
        return result;
    }
}
