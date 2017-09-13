package com.epam.lstrsum.model;

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
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@Setter
@Document(collection = Answer.ANSWER_COLLECTION_NAME)
public class Answer {
    public final static String ANSWER_COLLECTION_NAME = "Answer";

    @Id
    private String answerId;
    @DBRef(lazy = true)
    @Indexed
    private Question questionId;
    private String text;
    private Instant createdAt;

    @DBRef(lazy = true)
    private User authorId;

    private List<Vote> votes;
}
