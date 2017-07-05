package com.epam.lstrsum.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document
@AllArgsConstructor
@NoArgsConstructor
public class Answer {
    @Id
    private String answerId;
    private String parentId;
    private String[] tags;
    private String text;
    private String authorId;
    private Integer upVote;

}
