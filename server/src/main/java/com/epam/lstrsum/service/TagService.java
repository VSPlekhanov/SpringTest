package com.epam.lstrsum.service;

import com.epam.lstrsum.model.Question;
import lombok.RequiredArgsConstructor;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.data.domain.Sort.Direction.DESC;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Service
@RequiredArgsConstructor
public class TagService {
    private final MongoTemplate mongoTemplate;

    @Cacheable(value = "tagsRating", key = "'tags'")
    public List<String> getTagsRating() {
        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);

        return mongoTemplate.aggregate(
                newAggregation(
                        project("tags"),
                        unwind("tags"),
                        group("tags").count().as("n"),
                        sort(DESC, "n"),
                        project("tags")
                ),
                Question.QUESTION_COLLECTION_NAME, String.class).getMappedResults().stream().map(e -> {
            try {
                return ((JSONObject) parser.parse(e)).getAsString("_id");
            } catch (ParseException e1) {
                e1.printStackTrace();
            }
            return "";
        }).collect(Collectors.toList());
    }
}