package com.epam.lstrsum.service;

import com.epam.lstrsum.SetUpDataBaseCollections;
import lombok.extern.slf4j.Slf4j;
import net.sf.ehcache.CacheManager;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.number.OrderingComparison.greaterThanOrEqualTo;
import static org.mockito.Mockito.verify;

public class TagServiceTest extends SetUpDataBaseCollections {
    private static final int TAG_COUNT = 20;
    private static final String WANTED_NON_EXISTENT_TAG_BEGINNING = "javas_abc";
    private static final String WANTED_EXISTENT_TAG_BEGINNING = "javas";
    private static final String WANTED_EXISTENT_MANY_TAGS_BEGINNING = "jav";
    private static final String MOST_POPULAR_TAG = "javascript";
    private static final String NEXT_POPULAR_TAG = "java";

    @Autowired
    private CacheManager internalCacheManager;

    @Autowired
    private QuestionService questionService;

    @SpyBean
    private TagService tagService;


    @Test
    public void getZeroFilteredTagsByKeyword() {
        List<String> actualTags = tagService.getFilteredTagsRating(WANTED_NON_EXISTENT_TAG_BEGINNING);

        assertThat(actualTags.size(), is(0));
    }

    @Test
    public void getOneFilteredTagByKeyword() {
        List<String> actualTags = tagService.getFilteredTagsRating(WANTED_EXISTENT_TAG_BEGINNING);

        assertThat(actualTags.size(), is(1));
        assertThat(actualTags.get(0), is(MOST_POPULAR_TAG));
    }

    @Test
    public void getFewFilteredTagsByKeyword() throws Exception {
        List<String> actualTags = tagService.getFilteredTagsRating(WANTED_EXISTENT_MANY_TAGS_BEGINNING);

        assertThat(actualTags.size(), is(2));
        assertThat(actualTags.get(0), is(MOST_POPULAR_TAG));
        assertThat(actualTags.get(1), is(NEXT_POPULAR_TAG));
    }

    @Test
    public void getFilteredTagsRatingReturnAllTags() {
        List<String> actualTags = tagService.getFilteredTagsRating("");

        assertThat(actualTags.size(), greaterThanOrEqualTo(TAG_COUNT));
        assertThat(actualTags.get(0), is(MOST_POPULAR_TAG));
    }
}