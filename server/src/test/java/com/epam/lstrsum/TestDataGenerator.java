package com.epam.lstrsum;

import com.epam.lstrsum.model.Answer;
import com.epam.lstrsum.model.Request;
import com.epam.lstrsum.model.Subscription;
import com.epam.lstrsum.model.User;
import com.mongodb.BasicDBList;
import com.mongodb.DBObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TestDataGenerator {

    @Autowired
    private MongoTemplate mongoTemplate;

    private static List<User> users = new ArrayList<>(1000);
    private static List<Request> requests = new ArrayList<>(1000);
    private static List<Answer> answers = new ArrayList<>(1000);
    private static List<Subscription> subscriptions = new ArrayList<>(1000);

    @BeforeClass
    public static void generateCollectionLists() {
        for (int i = 0; i < 1000; i++) {
            final User authorOfRequest = buildAuthorOf(i);
            if (!users.contains(authorOfRequest)) {
                users.add(authorOfRequest);
            }

            final Request request = buildRequest(i, authorOfRequest);
            requests.add(request);
            final Answer answer = buildAnswer(i, request);
            answers.add(answer);
        }
        int i = 0;
        for (User u : users) {
            Subscription subscription = buildSubscription(i, u, requests);
            subscriptions.add(subscription);
            i++;
        }
    }

    @Ignore
    @Test
    public void generateAnswerCollectionTest() throws IOException {
        assertThat(answers.size(), is(1000));
        final DBObject dbObject = new BasicDBList();
        mongoTemplate.getConverter().write(answers, dbObject);
        final String dbAnswers = dbObject.toString().replace("_class\" : \"" + Answer.class.getCanonicalName() + "\" ,", "");
        final String dbAnswers2 = dbAnswers.replace(", \"_class\" : \"" + Answer.class.getCanonicalName() + "\"", "");
        Files.write(Paths.get("src/test/resources/data/generated/Answers.json"), dbAnswers2.getBytes());
    }

    @Ignore
    @Test
    public void generateRequestCollectionTest() throws IOException {
        assertThat(requests.size(), is(1000));
        final DBObject dbObject = new BasicDBList();
        mongoTemplate.getConverter().write(requests, dbObject);
        final String dbRequests = dbObject.toString().replace("_class\" : \"" + Request.class.getCanonicalName() + "\" ,", "");
        final String dbRequests2 = dbRequests.replace(", \"_class\" : \"" + Request.class.getCanonicalName() + "\"", "");
        Files.write(Paths.get("src/test/resources/data/generated/Requests.json"), dbRequests2.getBytes());
    }

    @Ignore
    @Test
    public void generateSubscriptionCollectionTest() throws IOException {
        assertThat(subscriptions.size(), is(1000));
        final DBObject dbObject = new BasicDBList();
        mongoTemplate.getConverter().write(subscriptions, dbObject);
        final String dbSubscriptions = dbObject.toString().replace("_class\" : \"" + Subscription.class.getCanonicalName() + "\" ,", "");
        final String dbSubscriptions2 = dbSubscriptions.replace(", \"_class\" : \"" + Subscription.class.getCanonicalName() + "\"", "");
        Files.write(Paths.get("src/test/resources/data/generated/Subscriptions.json"), dbSubscriptions2.getBytes());
    }

    @Ignore
    @Test
    public void generateUserCollectionTest() throws IOException {
        assertThat(users.size(), is(1000));
        final DBObject dbObject = new BasicDBList();
        mongoTemplate.getConverter().write(users, dbObject);
        final String dbUsers = dbObject.toString().replace("\"_class\" : " + User.class.getCanonicalName() + "\" ,", "");
        final String dbUsers2 = dbUsers.replace(", \"_class\" : \"" + User.class.getCanonicalName() + "\"", "");
        Files.write(Paths.get("src/test/resources/data/generated/Users.json"), dbUsers2.getBytes());
    }


    private static Answer buildAnswer(int numberOfAnswer, Request request) {
        int rndId = ThreadLocalRandom.current().nextInt(request.getAllowedSubs().size());
        return Answer.builder()
                .answerId("AnswerId" + numberOfAnswer)
                .parentId(request)
                .text("Some interesting answer for some interesting request" + numberOfAnswer)
                .createdAt(Instant.now())
                .authorId(request.getAllowedSubs().get(rndId))
                .upVote(ThreadLocalRandom.current().nextInt(15))
                .build();
    }

    private static Subscription buildSubscription(int numberOfSubscription, User user, List<Request> requests) {
        final Subscription subscription = new Subscription();
        subscription.setSubscriptionId("SubscriptionId" + numberOfSubscription);
        subscription.setUserId(user);
        final List<Request> subRequests = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            final Request request = requests.get(new Random().nextInt(1000));
            if (!subRequests.contains(request)) {
                subRequests.add(request);
            }
        }
        subscription.setRequestIds(subRequests);
        return subscription;
    }

    private static Request buildRequest(int numberOfRequest, User authorOfRequest) {
        final List<User> allowedSubs = IntStream.range(0, 21)
                .map(i -> ThreadLocalRandom.current().nextInt(1001))
                .mapToObj(TestDataGenerator::buildAuthorOf)
                .collect(Collectors.toList());

        return Request.builder()
                .requestId("RequestId" + numberOfRequest)
                .title("Some important for epam employees" + numberOfRequest)
                .tags(new String[]{"tag1" + numberOfRequest, "tag2" + numberOfRequest, "tag3" + numberOfRequest})
                .text("Some interesting text of request" + numberOfRequest)
                .createdAt(Instant.now())
                .deadLine(Instant.now())
                .authorId(authorOfRequest)
                .allowedSubs(allowedSubs)
                .upVote(21)
                .build();
    }

    @AfterClass
    public static void tearDown() {
        users = null;
        requests = null;
        answers = null;
        subscriptions = null;
    }

    private static User buildAuthorOf(int numberOfUser) {
        final User author = new User();
        author.setUserId("UserId" + numberOfUser);
        author.setFirstName("FirstName" + numberOfUser);
        author.setLastName("LastName" + numberOfUser);
        author.setEmail("UserId" + numberOfUser + "_" + "LastName" + numberOfUser + "@epam.com");
        author.setRoles(new String[]{"ADMIN, USER"});
        author.setCreatedAt(Instant.now());
        author.setIsActive(true);
        return author;
    }
}
