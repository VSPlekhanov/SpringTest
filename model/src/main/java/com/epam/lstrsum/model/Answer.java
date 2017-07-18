package com.epam.lstrsum.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = Answer.ANSWER_COLLECTION_NAME)
public class Answer {
    public final static String ANSWER_COLLECTION_NAME = "answer";

    @Id
    private String answerId;
    @DBRef
    private Request parentId;
    private String text;
    private Instant createdAt;

    @DBRef
    private User authorId;
    private Integer upVote;

}
