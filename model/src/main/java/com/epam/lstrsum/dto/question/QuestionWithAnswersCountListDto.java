package com.epam.lstrsum.dto.question;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class QuestionWithAnswersCountListDto {
    private final Long totalNumber;
    private final List<QuestionWithAnswersCountDto> questions;
}
