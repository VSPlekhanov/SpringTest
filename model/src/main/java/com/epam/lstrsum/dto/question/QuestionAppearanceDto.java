package com.epam.lstrsum.dto.question;

import com.epam.lstrsum.dto.attachment.AttachmentPropertiesDto;
import com.epam.lstrsum.dto.user.UserBaseDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;


@Getter
@Setter
@NoArgsConstructor
public class QuestionAppearanceDto extends QuestionBaseDto {
    private String text;
    private List<AttachmentPropertiesDto> attachments;

    public QuestionAppearanceDto(
            String questionId, String title, String[] tags, Instant createdAt, Instant deadLine,
            UserBaseDto author, String text
    ) {
        super(questionId, title, tags, createdAt, deadLine, author);
        this.text = text;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        QuestionAppearanceDto that = (QuestionAppearanceDto) o;
        if (!that.getAttachments().equals(this.getAttachments())) return false;
        return text != null ? text.equals(that.text) : that.text == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (text != null ? text.hashCode() : 0);
        return result;
    }
}
