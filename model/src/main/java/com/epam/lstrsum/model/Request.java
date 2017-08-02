package com.epam.lstrsum.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.TextScore;

import java.time.Instant;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@Document(collection = Request.REQUEST_COLLECTION_NAME)
public class Request {
    public final static String REQUEST_COLLECTION_NAME = "request";

    @Id
    private String requestId;

    private String title;
    private String[] tags;

    @TextIndexed
    private String text;
    private Instant createdAt;
    private Instant deadLine;

    @TextScore
    float score;

    @DBRef
    private User authorId;
    @DBRef
    private List<User> allowedSubs;
    private Integer upVote;
}
