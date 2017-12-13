package com.epam.lstrsum.service.impl;

import com.epam.lstrsum.dto.QueryErrorDefinitionDto;
import com.epam.lstrsum.dto.question.QuestionParsedQueryDto;
import com.epam.lstrsum.service.SearchQueryService;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@ConfigurationProperties(prefix = "elastic")
@Slf4j
public class SearchQueryServiceImpl implements SearchQueryService {

    private final String splitArrayPattern = "\\s*,\\s*";
    private static final Pattern validateQueryWithMetaTag = Pattern.compile("[^:\"]+");
    private final Pattern metaTagWithValuePattern =
            Pattern.compile("([^\\s\",]+):\\s*((((\"[^\",]+\")|([^\\s\",]+))\\s*,\\s*)*((\"[^\",]+\")|([^\\s\",]+)))\\s*");

    @Setter
    private List<String> validMetaTags;

    @Override
    public QuestionParsedQueryDto parseQuery(String query) {
        StringBuilder queryForSearch = new StringBuilder();
        List<QueryErrorDefinitionDto> errorsInQuery = new ArrayList<>();
        List<String> queryStringsWithMetaTags = new ArrayList<>();

        if (!query.isEmpty()) {
            Integer nextStart = 0;
            Matcher matcher = metaTagWithValuePattern.matcher(query);
            List<String> localValidMetaTags = new ArrayList<>(validMetaTags);

            while (matcher.find(nextStart)) {
                String metaTag = matcher.group(1).toLowerCase();
                if (!localValidMetaTags.contains(metaTag) || !validateValueOfMetaTag(matcher.group(2)))
                    errorsInQuery.add(new QueryErrorDefinitionDto(matcher.start(), matcher.end()));
                else {
                    queryStringsWithMetaTags.add(String.format("%s:(%s)", metaTag, matcher.group(2).replaceAll(",", " ")));
                    localValidMetaTags.remove(matcher.group(1));
                }
                queryForSearch.append(getValidQueryForSearch(query, nextStart, matcher.start(), errorsInQuery));
                nextStart = matcher.end();
            }
            queryForSearch.append(getValidQueryForSearch(query, nextStart, query.length(), errorsInQuery));
        }
        return new QuestionParsedQueryDto(queryStringsWithMetaTags, errorsInQuery, queryForSearch.toString().trim());
    }

    private String getValidQueryForSearch(String query, Integer from, Integer to, List<QueryErrorDefinitionDto> errorsInQuery) {
        String subQuery = query.substring(from, to).replaceAll("[\" ]+", " ").trim();
        if (Pattern.compile("[^:,]+").matcher(subQuery).matches()) return subQuery + ' ';
        else {
            if (!subQuery.equals("")) errorsInQuery.add((new QueryErrorDefinitionDto(from, to)));
            return "";
        }
    }

    private boolean validateValueOfMetaTag(String values) {
        String[] value = values.trim().replaceAll("\"", "").split(splitArrayPattern);
        for (String metaTag : value)
            if (!validateQueryWithMetaTag.matcher(metaTag.trim()).matches()) return false;
        return true;
    }

}