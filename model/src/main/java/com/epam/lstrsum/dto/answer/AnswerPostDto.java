package com.epam.lstrsum.dto.answer;


import com.epam.lstrsum.exception.ConvertToJsonException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;


@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Slf4j
public class AnswerPostDto {
    private String questionId;
    private String text;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AnswerPostDto postDto = (AnswerPostDto) o;

        if (questionId != null ? !questionId.equals(postDto.questionId) : postDto.questionId != null) return false;
        return text != null ? !text.equals(postDto.text) : postDto.text != null;
    }

    @Override
    public int hashCode() {
        int result = questionId != null ? questionId.hashCode() : 0;
        result = 31 * result + (text != null ? text.hashCode() : 0);
        return result;
    }

    public String toJson() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            log.error("QuestionPostDto toJson() exception, probably during question validation" + e.getMessage());
            throw new ConvertToJsonException("Can't convert answer to JSON!");
        }
    }
}
