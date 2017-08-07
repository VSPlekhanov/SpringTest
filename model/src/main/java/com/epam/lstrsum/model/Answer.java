package com.epam.lstrsum.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Builder
@Document(collection = Answer.ANSWER_COLLECTION_NAME)
public class Answer {
    public final static String ANSWER_COLLECTION_NAME = "answer";

    @Id
    private String answerId;
    @DBRef
    @Indexed
    private Question parentId;
    private String text;
    private Instant createdAt;

    @DBRef
    private User authorId;
    @Setter
    private Integer upVote;

}
