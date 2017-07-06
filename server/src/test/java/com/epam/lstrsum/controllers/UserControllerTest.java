package com.epam.lstrsum.controllers;

import com.epam.lstrsum.model.User;
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
public class UserControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private MongoTemplate mongoTemplate;



    @Before
    public void setUp() throws Exception {
        final ObjectMapper userMapper = new ObjectMapper();
        final List<User> users = userMapper.readValue(getClass().getResourceAsStream("/data/userLoad.json"),
                userMapper.getTypeFactory().constructCollectionType(List.class, User.class));
        for (User u : users) {
            mongoTemplate.save(u);
        }

    }

    @Test
    public void getListOfUsers() throws Exception {
        ResponseEntity<List<User>> responseEntity = restTemplate.exchange("/api/user",
                HttpMethod.GET, null, new ParameterizedTypeReference<List<User>>() {
                });
        List<User> actualList = responseEntity.getBody();
        //validate
        assertThat(actualList.size(), is(6));
        List<String> actualIds = actualList.stream().map(User::getUserId).collect(collectingAndThen(toList(), ImmutableList::copyOf));
        assertThat(actualIds, containsInAnyOrder("1u", "2u", "3u", "4u", "5u", "6u"));
    }

    @After
    public void tearDown() throws Exception {
        mongoTemplate.dropCollection(User.class);
    }
}