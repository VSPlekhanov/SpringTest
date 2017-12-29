package com.epam.lstrsum.dto.feedback;

import com.epam.lstrsum.exception.ConvertToJsonException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;

@Getter
@NoArgsConstructor
@Setter
@AllArgsConstructor
@Builder
@Slf4j
public class FeedbackPostDto implements Serializable {

    private String title;
    private String text;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        FeedbackPostDto that = (FeedbackPostDto) o;

        if (title == null || !title.equals(that.title)) {
            return false;
        }
        return text != null ? text.equals(that.text) : that.text == null;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + (title != null ? title.hashCode(): 0);
        result = 31 * result + (text != null ? text.hashCode() : 0);
        return result;
    }

    public String toJson() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            log.error("FeedbackPostDto toJson() exception, probably during feedback validation {}", e.getMessage());
            throw new ConvertToJsonException("Can't convert question to JSON!");
        }
    }
}
