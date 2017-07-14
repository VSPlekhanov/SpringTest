package com.epam.lstrsum.dto;


import com.epam.lstrsum.model.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

@AllArgsConstructor
@Getter
public class RequestAllFieldsDto {
    private String requestId;
    private String title;
    private String[] tags;
    private String text;
    private Instant createdAt;
    private Instant deadLine;
    private User authorId;
    private List<User> allowedSubs;
    private Integer upVote;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RequestAllFieldsDto that = (RequestAllFieldsDto) o;

        if (requestId != null ? !requestId.equals(that.requestId) : that.requestId != null) return false;
        if (title != null ? !title.equals(that.title) : that.title != null) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(tags, that.tags)) return false;
        if (text != null ? !text.equals(that.text) : that.text != null) return false;
        if (createdAt != null ? !createdAt.equals(that.createdAt) : that.createdAt != null) return false;
        if (deadLine != null ? !deadLine.equals(that.deadLine) : that.deadLine != null) return false;
        if (authorId != null ? !authorId.equals(that.authorId) : that.authorId != null) return false;
        if (allowedSubs != null ? !allowedSubs.equals(that.allowedSubs) : that.allowedSubs != null) return false;
        return upVote != null ? upVote.equals(that.upVote) : that.upVote == null;
    }

    @Override
    public int hashCode() {
        int result = requestId != null ? requestId.hashCode() : 0;
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(tags);
        result = 31 * result + (text != null ? text.hashCode() : 0);
        result = 31 * result + (createdAt != null ? createdAt.hashCode() : 0);
        result = 31 * result + (deadLine != null ? deadLine.hashCode() : 0);
        result = 31 * result + (authorId != null ? authorId.hashCode() : 0);
        result = 31 * result + (allowedSubs != null ? allowedSubs.hashCode() : 0);
        result = 31 * result + (upVote != null ? upVote.hashCode() : 0);
        return result;
    }
}
