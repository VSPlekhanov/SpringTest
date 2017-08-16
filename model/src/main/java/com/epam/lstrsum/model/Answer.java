package com.epam.lstrsum.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

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
    @DBRef
    @Indexed
    private Question questionId;
    private String text;
    private Instant createdAt;

    @DBRef
    private User authorId;

    @Setter
    private Integer upVote;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Answer answer = (Answer) o;

        if (answerId != null ? !answerId.equals(answer.answerId) : answer.answerId != null) return false;
        if (questionId != null ? !questionId.equals(answer.questionId) : answer.questionId != null) return false;
        if (!text.equals(answer.text)) return false;
        if (createdAt != null ? !createdAt.equals(answer.createdAt) : answer.createdAt != null) return false;
        if (authorId != null ? !authorId.equals(answer.authorId) : answer.authorId != null) return false;
        return upVote != null ? upVote.equals(answer.upVote) : answer.upVote == null;
    }

    @Override
    public int hashCode() {
        int result = answerId != null ? answerId.hashCode() : 0;
        result = 31 * result + (questionId != null ? questionId.hashCode() : 0);
        result = 31 * result + text.hashCode();
        result = 31 * result + (createdAt != null ? createdAt.hashCode() : 0);
        result = 31 * result + (authorId != null ? authorId.hashCode() : 0);
        result = 31 * result + (upVote != null ? upVote.hashCode() : 0);
        return result;
    }
}
