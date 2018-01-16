package com.epam.lstrsum.service;

import com.epam.lstrsum.dto.question.QuestionWithAnswersCountListDto;

import java.util.List;

public interface ElasticSearchService {
    /**
     * @param searchString search string to search by all fields in valid fields(meta tags) list, see properties
     * @param metaTags values of certain fields from valid fields(meta tags) list, see properties
     * @return json string with questions, satisfied the parameters; with highlights
     * @see <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/search-search.html">Elastic Search</a>
     */
    String advancedSearchGetString(String searchString, List<String> metaTags, Integer page, Integer size);

    /**
     * @param searchString search string to search by all fields in valid fields(meta tags) list, see properties
     * @param metaTags values of certain fields from valid fields(meta tags) list, see properties
     * @return special dto for ui with list of found questions
     * @see <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/search-search.html">Elastic Search</a>
     */
    QuestionWithAnswersCountListDto advancedSearchGetDtoWithHighlights(String searchString, List<String> metaTags, Integer page, Integer size);
}
