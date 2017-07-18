package com.epam.lstrsum.dto;

import com.epam.lstrsum.model.Request;
import com.epam.lstrsum.model.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@AllArgsConstructor
@Getter
public class AnswerAllFieldsDto {
    private String answerId;
    private Request parentId;
    private String text;
    private Instant createdAt;
    private User authorId;
    private Integer upVote;
}
