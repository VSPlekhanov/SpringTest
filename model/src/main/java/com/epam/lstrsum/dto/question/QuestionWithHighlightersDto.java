package com.epam.lstrsum.dto.question;

import com.epam.lstrsum.dto.user.UserBaseDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.Arrays;

@Getter
@Setter
@NoArgsConstructor
public class QuestionWithHighlightersDto extends  QuestionWithAnswersCountDto {
    private String[] highlightedText;

    public QuestionWithHighlightersDto(String questionId,
                                       String title,
                                       String[] tags,
                                       Instant createdAt,
                                       Instant deadLine,
                                       UserBaseDto authorId,
                                       Integer answersCount,
                                       String[] highlightedText) {
        super(questionId, title, tags, createdAt, deadLine, authorId, answersCount);
        this.highlightedText = highlightedText;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof QuestionWithHighlightersDto)) return false;
        if (!super.equals(o)) return false;

        QuestionWithHighlightersDto that = (QuestionWithHighlightersDto) o;

        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return Arrays.equals(highlightedText, that.highlightedText);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Arrays.hashCode(highlightedText);
        return result;
    }
}
