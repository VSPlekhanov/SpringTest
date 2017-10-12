package com.epam.lstrsum.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.TextScore;

import java.time.Instant;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@CompoundIndex(unique = true, def = "{'title': 1, 'authorId': 1}")
@Document(collection = Question.QUESTION_COLLECTION_NAME)
public class Question {
    public static final String QUESTION_COLLECTION_NAME = "Question";
    @TextScore
    float score;
    @Id
    private String questionId;

    private String title;
    private String[] tags;

    @TextIndexed
    private String text;

    private Instant createdAt;
    private Instant deadLine;
    private List<byte[]> inlineSources;
    @DBRef
    private User authorId;
    @DBRef(lazy = true)
    private List<User> allowedSubs;
    private List<String> attachmentIds;
    private List<Answer> answers;
}
