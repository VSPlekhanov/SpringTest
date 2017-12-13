package com.epam.lstrsum.service.impl;

import com.epam.lstrsum.dto.QueryErrorDefinitionDto;
import com.epam.lstrsum.dto.question.QuestionParsedQueryDto;
import com.epam.lstrsum.service.SearchQueryService;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class SearchQueryServiceTest {

    private SearchQueryService searchQueryService;

    @Before
    public void setUp(){
        searchQueryService = new SearchQueryServiceImpl();
        List<String> metaTags = new ArrayList<>();
        metaTags.add("title");
        metaTags.add("text");
        metaTags.add("tags");
        searchQueryService.setValidMetaTags(metaTags);
    }

    @Test
    public void parseQueryContainsOneValidTagWithValue() throws Exception {
        QuestionParsedQueryDto parseResult = searchQueryService.parseQuery("title: Name");
        assertThat(parseResult.getQueryStringsWithMetaTags(), containsInAnyOrder("title:(Name)"));
        assertTrue(parseResult.getErrorsInQuery().isEmpty());
    }

    @Test
    public void parseQueryContainsOneValidTagWithoutValue() throws Exception {
        QuestionParsedQueryDto parseResult = searchQueryService.parseQuery("title: ");
        assertThat(parseResult.getQueryStringsWithMetaTags(), not(containsInAnyOrder("title:()")));
        assertThat(parseResult.getErrorsInQuery().size(), is(1));
        assertThat(parseResult.getErrorsInQuery(), contains(new QueryErrorDefinitionDto(0, 7)));
    }

    @Test
    public void parseQueryContainsOneValidTagWithValues() throws Exception {
        QuestionParsedQueryDto parseResult = searchQueryService.parseQuery("tags: tag1, tag2, tag3");
        assertThat(parseResult.getQueryStringsWithMetaTags(), containsInAnyOrder("tags:(tag1  tag2  tag3)"));
        assertTrue(parseResult.getErrorsInQuery().isEmpty());
    }

    @Test
    public void parseQueryContainsAllValidTagsWithOneValue() throws Exception {
        QuestionParsedQueryDto parseResult = searchQueryService.parseQuery("text: Text title: Name tags: Tag1");
        assertThat(parseResult.getQueryStringsWithMetaTags(), containsInAnyOrder("text:(Text)", "title:(Name)", "tags:(Tag1)"));
        assertTrue(parseResult.getErrorsInQuery().isEmpty());
    }

    @Test
    public void parseQueryContainsUpperMetaTags() throws Exception {
        QuestionParsedQueryDto parseResult = searchQueryService.parseQuery("TEXT: Text tItle: Name taGS: Tag1");
        assertThat(parseResult.getQueryStringsWithMetaTags(), containsInAnyOrder("text:(Text)", "title:(Name)", "tags:(Tag1)"));
        assertTrue(parseResult.getErrorsInQuery().isEmpty());
    }

    @Test
    public void parseQueryContainsAllValidTagsWithOneFewOrNoValue() throws Exception {
        QuestionParsedQueryDto parseResult = searchQueryService.parseQuery("text: title: Name tags: Tag1, Tag2, Tag3, Tag4");
        assertThat(parseResult.getQueryStringsWithMetaTags(), not(containsInAnyOrder("text:()")));
        assertThat(parseResult.getQueryStringsWithMetaTags(), not(containsInAnyOrder("title:(Name)")));
        assertThat(parseResult.getQueryStringsWithMetaTags(), containsInAnyOrder("tags:(Tag1  Tag2  Tag3  Tag4)"));
        assertThat(parseResult.getErrorsInQuery().size(), is(1));
        assertThat(parseResult.getErrorsInQuery(), contains(new QueryErrorDefinitionDto(0, 13)));
    }

    @Test
    public void parseQueryContainsInvalidTag() throws Exception {
        QuestionParsedQueryDto parseResult = searchQueryService.parseQuery("text: Text invalidTag: invalidValue");
        assertThat(parseResult.getQueryStringsWithMetaTags(), containsInAnyOrder("text:(Text)"));
        assertThat(parseResult.getQueryStringsWithMetaTags(), not(containsInAnyOrder("invalidTag:(invalidValue)")));
        assertThat(parseResult.getErrorsInQuery().size(), is(1));
        assertThat(parseResult.getErrorsInQuery(), containsInAnyOrder(new QueryErrorDefinitionDto(11, 35)));
    }

    @Test
    public void parseQueryContainsDoubleValidTag() throws Exception {
        QuestionParsedQueryDto parseResult = searchQueryService.parseQuery("text: \"Text is valid\" text: \"Text is invalid\"");
        assertThat(parseResult.getQueryStringsWithMetaTags(), containsInAnyOrder("text:(\"Text is valid\")"));
        assertThat(parseResult.getQueryStringsWithMetaTags(), not(containsInAnyOrder("text:(\"Text is invalid\")")));
        assertThat(parseResult.getErrorsInQuery().size(), is(1));
        assertThat(parseResult.getErrorsInQuery(), contains(new QueryErrorDefinitionDto(22, 45)));
    }

    @Test
    public void parseQueryContainsValidValuesInQuotes() throws Exception {
        QuestionParsedQueryDto parseResult = searchQueryService.parseQuery("text: \"Text one in quotes\", simple, \"Text two in quotes\"");
        assertThat(parseResult.getQueryStringsWithMetaTags(), containsInAnyOrder("text:(\"Text one in quotes\"  simple  \"Text two in quotes\")"));
        assertTrue(parseResult.getErrorsInQuery().isEmpty());
    }

    //"\"text:\" \"value of tag: in quotes\"" does not work
    @Test
    public void parseQueryContainsInvalidTagInQuotes() throws Exception {
        QuestionParsedQueryDto parseResult = searchQueryService.parseQuery(
                        "\"title\": \"value of title in quotes\" " +
                               "\"text:\" \"value of tag in quotes\"");
        assertTrue(parseResult.getQueryStringsWithMetaTags().isEmpty());
        assertThat(parseResult.getErrorsInQuery().size(), is(2));
    }

    @Test
    public void parseQueryContainsInvalidValueInQuotes() throws Exception {
        QuestionParsedQueryDto parseResult = searchQueryService.parseQuery(
                "text: \"text: value\" " +
                "title:\"Text one, text two\" ");
        assertTrue(parseResult.getQueryStringsWithMetaTags().isEmpty());
        assertTrue(parseResult.getQueryStringsWithMetaTags().isEmpty());
        assertThat(parseResult.getQueryForSearch(), is(""));
        assertThat(parseResult.getErrorsInQuery().size(), is(2));
    }


    @Test
    public void parseQueryContainsTagAndValueInQuotes() throws Exception {
        QuestionParsedQueryDto parseResult = searchQueryService.parseQuery("\"tags: one\"");
        assertTrue(parseResult.getErrorsInQuery().isEmpty());
    }

    @Test
    public void parseQueryContainsInvalidValueWithPunctuationMarks() throws Exception {
        QuestionParsedQueryDto parseResult = searchQueryService.parseQuery("text: Te[xt query, with error marks! tags: \"Some text; ok?\"");
        assertThat(parseResult.getQueryStringsWithMetaTags(), not(containsInAnyOrder("text:(Te[xt)")));
        assertThat(parseResult.getQueryStringsWithMetaTags(), not(containsInAnyOrder("tags:(\"Some text, ok?\")")));
        assertThat(parseResult.getQueryForSearch(), isEmptyString());
        assertThat(parseResult.getErrorsInQuery().size(), is(1));
    }

    @Test
    public void parseInvalidQueryWithColon() throws Exception {
        QuestionParsedQueryDto parseResult = searchQueryService.parseQuery(
            "title: title: \"invalid title\" " +
                "title:: \"Chapter1\" " +
                "title: \"title: Chapter1\" " +
                "title: Chap:ter " +
                "text: text: thing ");

        assertTrue(parseResult.getQueryStringsWithMetaTags().isEmpty());
        assertThat(parseResult.getQueryForSearch(), is("invalid title thing"));
        assertFalse(parseResult.getErrorsInQuery().isEmpty());
    }

    @Test
    public void parseSimpleSearchString() throws Exception {
        String query = "Simple query (without tags)";

        QuestionParsedQueryDto parseResult = searchQueryService.parseQuery(query);
        assertThat(parseResult.getQueryForSearch(), is(query));
        assertTrue(parseResult.getErrorsInQuery().isEmpty());
    }

    @Test
    public void parseQueryWithSpacesAndSearchString() throws Exception {
        String query = "title: valueTitle " +
                "first query aaa " +
                "secondQuery! " +
                "tags:valueTags1  , valueTags2,valueTags3 ,valueTags4 " +
                "third query some query String " +
                "text:  valueText " +
                "last query";
        QuestionParsedQueryDto parseResult = searchQueryService.parseQuery(query);
        assertThat(parseResult.getQueryStringsWithMetaTags(), containsInAnyOrder(
                "title:(valueTitle)",
                "tags:(valueTags1    valueTags2 valueTags3  valueTags4)",
                "text:(valueText)")
        );
        assertThat(parseResult.getQueryForSearch(), is("first query aaa secondQuery! third query some query String last query"));
        assertTrue(parseResult.getErrorsInQuery().isEmpty());
    }


}