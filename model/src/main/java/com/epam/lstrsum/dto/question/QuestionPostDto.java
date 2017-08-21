package com.epam.lstrsum.dto.question;

import com.epam.lstrsum.exception.ConvertToJsonException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

@Getter
@NoArgsConstructor
@Setter
@AllArgsConstructor
@Slf4j
@Builder
public class QuestionPostDto implements Serializable {
    private String title;
    private String[] tags;
    private String text;
    private Long deadLine;

    private List<String> allowedSubs;

    private List<byte[]> inlineSources;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        QuestionPostDto that = (QuestionPostDto) o;

        if (!title.equals(that.title)) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(tags, that.tags)) return false;
        if (text != null ? !text.equals(that.text) : that.text != null) return false;
        if (deadLine != null ? !deadLine.equals(that.deadLine) : that.deadLine != null) return false;
        if (!allowedSubs.equals(that.allowedSubs)) return false;
        return inlineSources.equals(that.inlineSources);
    }

    @Override
    public int hashCode() {
        int result = title.hashCode();
        result = 31 * result + Arrays.hashCode(tags);
        result = 31 * result + (text != null ? text.hashCode() : 0);
        result = 31 * result + (deadLine != null ? deadLine.hashCode() : 0);
        result = 31 * result + allowedSubs.hashCode();
        result = 31 * result + inlineSources.hashCode();
        return result;
    }

    public String toJson() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            log.error("QuestionPostDto toJson() exception, probably during question validation" + e.getMessage());
            throw new ConvertToJsonException("Can't convert question to JSON!");
        }
    }
}
