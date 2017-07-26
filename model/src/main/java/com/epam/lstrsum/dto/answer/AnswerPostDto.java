package com.epam.lstrsum.dto.answer;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;


@AllArgsConstructor
@Getter
public class AnswerPostDto {
    private String parentId;
    private String text;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AnswerPostDto postDto = (AnswerPostDto) o;

        if (parentId != null ? !parentId.equals(postDto.parentId) : postDto.parentId != null) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return text != null ? !text.equals(postDto.text) : postDto.text != null;
    }

    @Override
    public int hashCode() {
        int result = parentId != null ? parentId.hashCode() : 0;
        result = 31 * result + (text != null ? text.hashCode() : 0);
        return result;
    }

    public String toJson() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return "It happens";
        }
    }
}
