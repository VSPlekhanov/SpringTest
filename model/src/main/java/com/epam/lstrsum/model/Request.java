package com.epam.lstrsum.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Document
public class Request {
    @Id
    private String requestId;
    private String title;
    private String[] tags;
    private String text;
    private String authorId;
    private Integer upVote;
    private String[] allowedSubs;
    private String deadLine;
}
