package com.epam.lstrsum.service.impl;

import com.epam.lstrsum.model.Question;
import com.epam.lstrsum.service.TagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.data.domain.Sort.Direction.DESC;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.sort;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.unwind;

@Service
@RequiredArgsConstructor
@Slf4j
public class TagServiceImpl implements TagService {
    private final MongoTemplate mongoTemplate;
    private JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);

    @Override
    public List<String> getTagsRating() {
        Aggregation aggregation = newAggregation(
                project("tags"),
                unwind("tags"),
                group("tags").count().as("n"),
                sort(DESC, "n"),
                project("tags")
        );

        return mongoTemplate.aggregate(aggregation, Question.QUESTION_COLLECTION_NAME, String.class)
                .getMappedResults()
                .stream()
                .map(this::parseTag).collect(Collectors.toList());
    }

    private String parseTag(String tag) {
        try {
            return parser.parse(tag, JSONObject.class).getAsString("_id");
        } catch (ParseException e1) {
            log.warn("Can't parse tag. Gotten json = {}", tag);
            return "";
        }
    }
}