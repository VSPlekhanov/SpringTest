package com.epam.lstrsum.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@Getter
@Builder
@Document(collection = Vote.VOTE_COLLECTION_NAME)
public class Vote {
    public final static String VOTE_COLLECTION_NAME = "Vote";

    @Id
    private String voteId;
    private Instant createdAt;
    @DBRef
    private User userId;
    @DBRef
    private Answer answerId;
    @Setter
    private boolean isRevoked;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Vote vote = (Vote) o;

        if (isRevoked != vote.isRevoked) return false;
        if (voteId != null ? !voteId.equals(vote.voteId) : vote.voteId != null) return false;
        if (createdAt != null ? !createdAt.equals(vote.createdAt) : vote.createdAt != null) return false;
        if (userId != null ? !userId.equals(vote.userId) : vote.userId != null) return false;
        return answerId != null ? answerId.equals(vote.answerId) : vote.answerId == null;
    }

    @Override
    public int hashCode() {
        int result = voteId != null ? voteId.hashCode() : 0;
        result = 31 * result + (createdAt != null ? createdAt.hashCode() : 0);
        result = 31 * result + (userId != null ? userId.hashCode() : 0);
        result = 31 * result + (answerId != null ? answerId.hashCode() : 0);
        result = 31 * result + (isRevoked ? 1 : 0);
        return result;
    }
}
