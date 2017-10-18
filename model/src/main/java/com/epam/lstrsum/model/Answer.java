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
//@Document(collection = Answer.ANSWER_COLLECTION_NAME)
public class Answer {
    private String answerId;    // must be initialized via new ObjectId()
    private String text;
    private Instant createdAt;
    private String authorId;    // another variant: private User author;    // TODO: 10.10.17 which variant is better?
    private List<Vote> votes;
}
