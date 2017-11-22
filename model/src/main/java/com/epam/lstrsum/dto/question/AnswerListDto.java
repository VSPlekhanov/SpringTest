package com.epam.lstrsum.dto.question;

import com.epam.lstrsum.dto.answer.AnswerBaseDto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class AnswerListDto {
    private final Long totalNumber;
    private final List<AnswerBaseDto> answers;
}
