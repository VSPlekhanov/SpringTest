package com.epam.lstrsum.dto.question;

import com.epam.lstrsum.dto.user.UserBaseDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;


@Getter
@NoArgsConstructor
@Setter
public class QuestionAppearanceDto extends QuestionBaseDto {
    private String text;
    private String[] attachmentIds;

    public QuestionAppearanceDto(
            String questionId, String title, String[] tags, Instant createdAt, Instant deadLine,
            UserBaseDto authorId, String text, String[] attachmentIds
    ) {
        super(questionId, title, tags, createdAt, deadLine, authorId);
        this.text = text;
        this.attachmentIds = attachmentIds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        QuestionAppearanceDto that = (QuestionAppearanceDto) o;
        if (!that.getAttachmentIds().equals(this.getAttachmentIds())) return false;
        return text != null ? text.equals(that.text) : that.text == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (text != null ? text.hashCode() : 0);
        return result;
    }
}
