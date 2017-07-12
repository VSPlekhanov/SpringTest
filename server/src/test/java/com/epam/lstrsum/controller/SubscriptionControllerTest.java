package com.epam.lstrsum.controller;

import com.epam.lstrsum.model.Subscription;
import com.epam.lstrsum.model.User;
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
public class SubscriptionControllerTest extends SetUpDataBaseCollections {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Test
    public void getListOfSubscriptions() throws Exception {
        final ResponseEntity<List<Subscription>> responseEntity = testRestTemplate.exchange("/api/subscription",
                HttpMethod.GET, null, new ParameterizedTypeReference<List<Subscription>>() {
                });
        List<Subscription> actualList = responseEntity.getBody();
        //validate
        assertThat(actualList.size(), is(6));
        List<String> actualIds = actualList.stream().map(Subscription::getUserId).map(User::getUserId).collect(collectingAndThen(toList(), ImmutableList::copyOf));
        assertThat(actualIds, containsInAnyOrder("1u", "2u", "3u", "4u", "5u", "6u"));
    }
}