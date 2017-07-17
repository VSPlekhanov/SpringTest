package com.epam.lstrsum.controller;

import com.epam.lstrsum.model.Answer;
import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AnswerControllerTest extends SetUpDataBaseCollections {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Test
    public void getListOfAnswers() throws Exception {
        final ResponseEntity<List<Answer>> responseEntity = testRestTemplate.exchange("/api/answer",
                HttpMethod.GET, null, new ParameterizedTypeReference<List<Answer>>() {
                });
        List<Answer> actualList = responseEntity.getBody();
        //validate
        assertThat(actualList.size(), is(12));
        List<String> actualIds = actualList.stream().map(Answer::getAnswerId).collect(collectingAndThen(toList(), ImmutableList::copyOf));
        assertThat(actualIds, containsInAnyOrder("1u_1r_1a", "1u_1r_2a", "1u_1r_3a", "1u_2r_1a", "1u_2r_2a",
                "2u_3r_1a", "2u_3r_2a", "3u_4r_1a", "3u_4r_2a", "4u_5r_1a", "4u_5r_2a", "6u_6r_1a"));
    }
}