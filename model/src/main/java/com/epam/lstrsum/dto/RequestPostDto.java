package com.epam.lstrsum.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

@Getter
@AllArgsConstructor
public class RequestPostDto implements Serializable {
    private String title;
    private String[] tags;
    private String text;
    private String deadLine;
    private List<String> allowedSubs;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RequestPostDto postDto = (RequestPostDto) o;

        if (title != null ? !title.equals(postDto.title) : postDto.title != null) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(tags, postDto.tags)) return false;
        if (text != null ? !text.equals(postDto.text) : postDto.text != null) return false;
        if (deadLine != null ? !deadLine.equals(postDto.deadLine) : postDto.deadLine != null) return false;
        return allowedSubs != null ? allowedSubs.equals(postDto.allowedSubs) : postDto.allowedSubs == null;
    }

    @Override
    public int hashCode() {
        int result = title != null ? title.hashCode() : 0;
        result = 31 * result + Arrays.hashCode(tags);
        result = 31 * result + (text != null ? text.hashCode() : 0);
        result = 31 * result + (deadLine != null ? deadLine.hashCode() : 0);
        result = 31 * result + (allowedSubs != null ? allowedSubs.hashCode() : 0);
        return result;
    }

    public String toJson() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return "It will never happen";
        }
    }
}
