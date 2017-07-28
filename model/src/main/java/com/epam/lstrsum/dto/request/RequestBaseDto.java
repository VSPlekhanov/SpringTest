package com.epam.lstrsum.dto.request;


import com.epam.lstrsum.dto.user.UserBaseDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.Arrays;

@AllArgsConstructor
@Getter
public class RequestBaseDto {
    private String requestId;
    private String title;
    private String[] tags;
    private Instant createdAt;
    private Instant deadLine;
    private UserBaseDto author;
    private Integer upVote;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RequestBaseDto that = (RequestBaseDto) o;

        if (requestId != null ? !requestId.equals(that.requestId) : that.requestId != null) return false;
        if (title != null ? !title.equals(that.title) : that.title != null) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(tags, that.tags)) return false;
        if (createdAt != null ? !createdAt.equals(that.createdAt) : that.createdAt != null) return false;
        if (deadLine != null ? !deadLine.equals(that.deadLine) : that.deadLine != null) return false;
        if (author != null ? !author.equals(that.author) : that.author != null) return false;
        return upVote != null ? upVote.equals(that.upVote) : that.upVote == null;
    }

    @Override
    public int hashCode() {
        int result = requestId != null ? requestId.hashCode() : 0;
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(tags);
        result = 31 * result + (createdAt != null ? createdAt.hashCode() : 0);
        result = 31 * result + (deadLine != null ? deadLine.hashCode() : 0);
        result = 31 * result + (author != null ? author.hashCode() : 0);
//        result = 31 * result + (answers != null ? answers.hashCode() : 0);
        result = 31 * result + (upVote != null ? upVote.hashCode() : 0);
        return result;
    }
}
