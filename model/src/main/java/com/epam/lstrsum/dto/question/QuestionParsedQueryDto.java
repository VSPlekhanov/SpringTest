package com.epam.lstrsum.dto.question;

import com.epam.lstrsum.dto.QueryErrorDefinitionDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class QuestionParsedQueryDto {
    private List<String> queryStringsWithMetaTags;
    private List<QueryErrorDefinitionDto> errorsInQuery;
    private String queryForSearch;
}