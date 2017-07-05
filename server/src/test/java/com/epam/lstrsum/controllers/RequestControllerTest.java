package com.epam.lstrsum.controllers;

import com.epam.lstrsum.model.Request;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import org.junit.After;
import org.junit.Before;
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
public class RequestControllerTest {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private MongoTemplate mongoTemplate;


    @Before
    public void setUp() throws Exception {
        final ObjectMapper requestMapper = new ObjectMapper();
        final List<Request> requests = requestMapper.readValue(getClass().getResourceAsStream("/data/requestLoad.json"),
                requestMapper.getTypeFactory().constructCollectionType(List.class, Request.class));
        for (Request r : requests) {
            mongoTemplate.save(r);
        }

    }

    @Test
    public void getListOfRequests() throws Exception {
        final ResponseEntity<List<Request>> responseEntity = testRestTemplate.exchange("/api/request",
                HttpMethod.GET, null, new ParameterizedTypeReference<List<Request>>() {
                });
        final List<Request> actualList = responseEntity.getBody();
        //validate
        assertThat(actualList.size(), is(6));
        final List<String> actualIds = actualList.stream().map(Request::getRequestId).collect(collectingAndThen(toList(), ImmutableList::copyOf));
        assertThat(actualIds, containsInAnyOrder("1u_1r", "1u_2r", "2u_3r", "3u_4r", "4u_5r", "6u_6r"));
    }

    @After
    public void tearDown() throws Exception {
        mongoTemplate.dropCollection(Request.class);
    }
}