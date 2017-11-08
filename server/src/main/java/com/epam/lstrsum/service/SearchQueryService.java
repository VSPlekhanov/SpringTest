package com.epam.lstrsum.service;


import com.epam.lstrsum.dto.question.QuestionParsedQueryDto;

import java.util.List;

public interface SearchQueryService {
    QuestionParsedQueryDto parseQuery(String query);
    void setValidMetaTags(List<String> tags);
}
