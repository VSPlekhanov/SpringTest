package com.epam.lstrsum.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Document(collection = Request.REQUEST_COLLECTION_NAME)
public class Request {
    public final static String REQUEST_COLLECTION_NAME = "request";

    @Id
    private String requestId;
    private String title;
    private String[] tags;
    private String text;
    private Instant createdAt;
    private Instant deadLine;

    @DBRef
    private User authorId;
    @DBRef
    private List<User> allowedSubs;
    private Integer upVote;
}
