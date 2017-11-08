package com.epam.lstrsum.service.impl;

import com.epam.lstrsum.model.Question;
import com.epam.lstrsum.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.springframework.data.domain.Sort.Direction.DESC;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.limit;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.sort;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.unwind;
import static org.springframework.data.mongodb.core.query.Criteria.where;

@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {
    private final MongoTemplate mongoTemplate;

    private static class Tag {
        String _id;
    }

    private final static int MAX_TAGS_COUNT = 8;

    @Override
    public List<String> getFilteredTagsRating(String key) {
        Aggregation aggregation = newAggregation(
                project("tags"),
                unwind("tags"),
                match(where("tags").regex(Pattern.compile("^" + key))),
                group("tags").count().as("n"),
                sort(DESC, "n"),
                limit(MAX_TAGS_COUNT)
        );

        return mongoTemplate.aggregate(aggregation, Question.QUESTION_COLLECTION_NAME, Tag.class)
                .getMappedResults()
                .stream()
                .map(tag -> tag._id).collect(Collectors.toList());
    }
}