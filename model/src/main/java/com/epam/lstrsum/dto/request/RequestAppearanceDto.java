package com.epam.lstrsum.dto.request;

import com.epam.lstrsum.dto.answer.AnswerBaseDto;
import com.epam.lstrsum.dto.user.UserBaseDto;
import lombok.Getter;

import java.time.Instant;
import java.util.List;


@Getter
public class RequestAppearanceDto extends RequestBaseDto {
    private String text;
    private List<AnswerBaseDto> answers;

    public RequestAppearanceDto(String requestId, String title, String[] tags, Instant createdAt, Instant deadLine,
                                UserBaseDto authorId, Integer upVote, String text, List<AnswerBaseDto> answers) {
        super(requestId, title, tags, createdAt, deadLine, authorId, upVote);
        this.text = text;
        this.answers = answers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        RequestAppearanceDto that = (RequestAppearanceDto) o;

        if (text != null ? !text.equals(that.text) : that.text != null) return false;
        return answers != null ? answers.equals(that.answers) : that.answers == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (text != null ? text.hashCode() : 0);
        result = 31 * result + (answers != null ? answers.hashCode() : 0);
        return result;
    }
}
