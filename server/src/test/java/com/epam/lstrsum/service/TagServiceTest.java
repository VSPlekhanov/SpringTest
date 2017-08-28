package com.epam.lstrsum.service;

import com.epam.lstrsum.SetUpDataBaseCollections;
import com.epam.lstrsum.dto.question.QuestionPostDto;
import lombok.extern.slf4j.Slf4j;
import net.sf.ehcache.CacheManager;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.util.Collections;
import java.util.List;

import static com.epam.lstrsum.testutils.InstantiateUtil.someString;
import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.hamcrest.number.OrderingComparison.greaterThanOrEqualTo;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.verify;

@Slf4j
public class TagServiceTest extends SetUpDataBaseCollections {
    private static final int TAG_COUNT = 20;
    private static final String MOST_POPULAR_TAG = "javascript";

    @Autowired
    private CacheManager internalCacheManager;

    @Autowired
    private QuestionService questionService;

    @SpyBean
    private TagService tagService;

    @Test
    public void cacheWorks() {
        questionService.getRelevantTags("");
        questionService.getRelevantTags("");

        verify(tagService, atMost(1)).getTagsRating();
    }

    @Test
    public void getTagsRatingReturnExpectedValue() throws Exception {
        List<String> actualTags = tagService.getTagsRating();

        assertThat(actualTags.size(), greaterThanOrEqualTo(TAG_COUNT));
        assertThat(actualTags.get(0), is(MOST_POPULAR_TAG));
    }

    @Test
    public void getAllTagsCacheWorksOk() {
        internalCacheManager.clearAll();
        final int beforeAddTags = tagService.getTagsRating().size();

        final String newTag = "newUniqueTag";
        questionService.addNewQuestion(
                new QuestionPostDto(someString(), new String[]{newTag},
                        someString(), 1L, Collections.singletonList("Bob_Hoplins@epam.com"), emptyList()),
                "Bob_Hoplins@epam.com");

        final int afterAddTags = tagService.getTagsRating().size();
        assertThat(afterAddTags, greaterThan(beforeAddTags));
    }
}