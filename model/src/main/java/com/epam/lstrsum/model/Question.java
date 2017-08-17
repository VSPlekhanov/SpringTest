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
import java.util.Arrays;
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
    @DBRef
    private User authorId;
    @DBRef
    private List<User> allowedSubs;

    private List<String> attachmentIds;

    private Integer upVote;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Question question = (Question) o;

        if (Float.compare(question.score, score) != 0) return false;
        if (questionId != null ? !questionId.equals(question.questionId) : question.questionId != null) return false;
        if (!title.equals(question.title)) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(tags, question.tags)) return false;
        if (!text.equals(question.text)) return false;
        if (createdAt != null ? !createdAt.equals(question.createdAt) : question.createdAt != null) return false;
        if (deadLine != null ? !deadLine.equals(question.deadLine) : question.deadLine != null) return false;
        if (authorId != null ? !authorId.equals(question.authorId) : question.authorId != null) return false;
        if (allowedSubs != null ? !allowedSubs.equals(question.allowedSubs) : question.allowedSubs != null)
            return false;
        if (attachmentIds != null ? !attachmentIds.equals(question.attachmentIds) : question.attachmentIds != null)
            return false;
        return upVote != null ? upVote.equals(question.upVote) : question.upVote == null;
    }

    @Override
    public int hashCode() {
        int result = questionId != null ? questionId.hashCode() : 0;
        result = 31 * result + title.hashCode();
        result = 31 * result + Arrays.hashCode(tags);
        result = 31 * result + text.hashCode();
        result = 31 * result + (createdAt != null ? createdAt.hashCode() : 0);
        result = 31 * result + (deadLine != null ? deadLine.hashCode() : 0);
        result = 31 * result + (score != +0.0f ? Float.floatToIntBits(score) : 0);
        result = 31 * result + (authorId != null ? authorId.hashCode() : 0);
        result = 31 * result + (allowedSubs != null ? allowedSubs.hashCode() : 0);
        result = 31 * result + (attachmentIds != null ? attachmentIds.hashCode() : 0);
        result = 31 * result + (upVote != null ? upVote.hashCode() : 0);
        return result;
    }
}
