package com.epam.lstrsum.dto.answer;


import lombok.AllArgsConstructor;
import lombok.Getter;


@AllArgsConstructor
@Getter
public class AnswerPostDto {
    private String parentId;
    private String text;
}