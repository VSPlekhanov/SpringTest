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
    public final static String VOTE_COLLECTION_NAME = "vote";

    @Id
    private String voteId;
    private Instant createdAt;
    @DBRef
    private User userId;
    @DBRef
    private Answer answerId;
    @Setter
    private Boolean isRevoked;

}
