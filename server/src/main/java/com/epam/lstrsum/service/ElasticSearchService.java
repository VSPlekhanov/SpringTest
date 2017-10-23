package com.epam.lstrsum.service;

import com.epam.lstrsum.model.Question;

public interface ElasticSearchService {
    String ENDPOINT = "/%s/" + Question.QUESTION_COLLECTION_NAME + "/_search";
    String QUESTION_SEARCH =
            "{\"query\": {\"multi_match\":" +
                    "{\"query\": \"%s\"" +
                    ",\"fields\": [ \"title^2\", \"text\", \"tags^2\" ]," +
                    "\"fuzziness\": \"AUTO\",\"type\": \"most_fields\"}}," +
                    "\"highlight\" : {\"fields\" : {\"text\":{},\"tags\":{},\"title\":{}}}" +
                    "}";

    /**
     * @param searchQuery search query to elastic search in json
     * @return json string, have most relevant questions with highlights
     * @see <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/search-search.html">Elastic Search</a>
     */
    String smartSearch(String searchQuery, int page, int size);

    String smartSearchWithAllowedSub(String searchQuery, int page, int size, String email);
}
