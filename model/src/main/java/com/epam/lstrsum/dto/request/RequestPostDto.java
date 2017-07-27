package com.epam.lstrsum.dto.request;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

@Getter
@AllArgsConstructor
@Slf4j
public class RequestPostDto implements Serializable {
    private String title;
    private String[] tags;
    private String text;
    private Long deadLine;
    private List<String> allowedSubs;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RequestPostDto that = (RequestPostDto) o;

        if (title != null ? !title.equals(that.title) : that.title != null) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(tags, that.tags)) return false;
        if (text != null ? !text.equals(that.text) : that.text != null) return false;
        if (deadLine != null ? !deadLine.equals(that.deadLine) : that.deadLine != null) return false;
        return allowedSubs != null ? allowedSubs.equals(that.allowedSubs) : that.allowedSubs == null;
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
            log.error("RequestPostDto toJson() exception, probably during request validation" + e.getMessage());
            return "Error has occurred";
        }
    }
}
