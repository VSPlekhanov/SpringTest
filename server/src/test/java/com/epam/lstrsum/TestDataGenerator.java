package com.epam.lstrsum;

import com.epam.lstrsum.model.Answer;
import com.epam.lstrsum.model.Question;
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
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@Ignore
@RunWith(SpringRunner.class)
@SpringBootTest
public class TestDataGenerator {
    private static final int ALL_LISTS_INIT_CAPACITY = 1000;

    @Autowired
    private MongoTemplate mongoTemplate;

    private static List<User> users = new ArrayList<>(ALL_LISTS_INIT_CAPACITY);
    private static List<Question> questions = new ArrayList<>(ALL_LISTS_INIT_CAPACITY);
    private static List<Answer> answers = new ArrayList<>(ALL_LISTS_INIT_CAPACITY);
    private static List<Subscription> subscriptions = new ArrayList<>(ALL_LISTS_INIT_CAPACITY);

    @BeforeClass
    public static void generateCollectionLists() {
        for (int i = 0; i < ALL_LISTS_INIT_CAPACITY; i++) {
            final User authorOfQuestion = buildAuthorOf(i);
            if (!users.contains(authorOfQuestion)) {
                users.add(authorOfQuestion);
            }

            final Question question = buildQuestion(i, authorOfQuestion);
            questions.add(question);
            final Answer answer = buildAnswer(i, question);
            answers.add(answer);
        }
        int i = 0;
        for (User u : users) {
            Subscription subscription = buildSubscription(i, u, questions);
            subscriptions.add(subscription);
            i++;
        }
    }

    @Ignore
    @Test
    public void generateAnswerCollectionTest() throws IOException {
        assertThat(answers.size(), is(ALL_LISTS_INIT_CAPACITY));
        final DBObject dbObject = new BasicDBList();
        mongoTemplate.getConverter().write(answers, dbObject);
        final String dbAnswers = dbObject.toString().replace("_class\" : \"" + Answer.class.getCanonicalName() + "\" ,", "");
        final String dbAnswers2 = dbAnswers.replace(", \"_class\" : \"" + Answer.class.getCanonicalName() + "\"", "");
        Files.write(Paths.get("src/test/resources/data/generated/Answers.json"), dbAnswers2.getBytes());
    }

    @Ignore
    @Test
    public void generateQuestionCollectionTest() throws IOException {
        assertThat(questions.size(), is(ALL_LISTS_INIT_CAPACITY));
        final DBObject dbObject = new BasicDBList();
        mongoTemplate.getConverter().write(questions, dbObject);
        final String dbQuestions = dbObject.toString().replace("_class\" : \"" + Question.class.getCanonicalName() + "\" ,", "");
        final String dbQuestion2 = dbQuestions.replace(", \"_class\" : \"" + Question.class.getCanonicalName() + "\"", "");
        Files.write(Paths.get("src/test/resources/data/generated/Questions.json"), dbQuestion2.getBytes());
    }

    @Ignore
    @Test
    public void generateSubscriptionCollectionTest() throws IOException {
        assertThat(subscriptions.size(), is(ALL_LISTS_INIT_CAPACITY));
        final DBObject dbObject = new BasicDBList();
        mongoTemplate.getConverter().write(subscriptions, dbObject);
        final String dbSubscriptions = dbObject.toString().replace("_class\" : \"" + Subscription.class.getCanonicalName() + "\" ,", "");
        final String dbSubscriptions2 = dbSubscriptions.replace(", \"_class\" : \"" + Subscription.class.getCanonicalName() + "\"", "");
        Files.write(Paths.get("src/test/resources/data/generated/Subscriptions.json"), dbSubscriptions2.getBytes());
    }

    @Ignore
    @Test
    public void generateUserCollectionTest() throws IOException {
        assertThat(users.size(), is(ALL_LISTS_INIT_CAPACITY));
        final DBObject dbObject = new BasicDBList();
        mongoTemplate.getConverter().write(users, dbObject);
        final String dbUsers = dbObject.toString().replace("\"_class\" : " + User.class.getCanonicalName() + "\" ,", "");
        final String dbUsers2 = dbUsers.replace(", \"_class\" : \"" + User.class.getCanonicalName() + "\"", "");
        Files.write(Paths.get("src/test/resources/data/generated/Users.json"), dbUsers2.getBytes());
    }


    private static Answer buildAnswer(int numberOfAnswer, Question question) {
        int rndId = ThreadLocalRandom.current().nextInt(question.getAllowedSubs().size());
        return Answer.builder()
                .answerId("AnswerId" + numberOfAnswer)
                .questionId(question)
                .text("Some interesting answer for some interesting question" + numberOfAnswer)
                .createdAt(Instant.now())
                .authorId(question.getAllowedSubs().get(rndId))
                .upVote(ThreadLocalRandom.current().nextInt(15))
                .build();
    }

    private static Subscription buildSubscription(int numberOfSubscription, User user, List<Question> questions) {
        return Subscription.builder()
                .subscriptionId("SubscriptionId" + numberOfSubscription)
                .userId(user)
                .questionIds(
                        IntStream.range(0, 20)
                                .map(i -> ThreadLocalRandom.current().nextInt(ALL_LISTS_INIT_CAPACITY))
                                .distinct()
                                .mapToObj(questions::get)
                                .collect(Collectors.toList())
                )
                .build();
    }

    private static Question buildQuestion(int numberOfQuestion, User authorOfQuestion) {
        final List<User> allowedSubs = IntStream.range(0, 21)
                .map(i -> ThreadLocalRandom.current().nextInt(1001))
                .mapToObj(TestDataGenerator::buildAuthorOf)
                .collect(Collectors.toList());

        return Question.builder()
                .questionId("QuestionId" + numberOfQuestion)
                .title("Some important for epam employees" + numberOfQuestion)
                .tags(new String[]{"tag1" + numberOfQuestion, "tag2" + numberOfQuestion, "tag3" + numberOfQuestion})
                .text("Some interesting text of question" + numberOfQuestion)
                .createdAt(Instant.now())
                .deadLine(Instant.now())
                .authorId(authorOfQuestion)
                .allowedSubs(allowedSubs)
                .upVote(21)
                .build();
    }

    @AfterClass
    public static void tearDown() {
        users = null;
        questions = null;
        answers = null;
        subscriptions = null;
    }

    private static User buildAuthorOf(int numberOfUser) {
        return User.builder()
                .userId("UserId" + numberOfUser)
                .firstName("FirstName" + numberOfUser)
                .lastName("LastName" + numberOfUser)
                .email("UserId" + numberOfUser + "_" + "LastName" + numberOfUser + "@epam.com")
                .roles(new String[]{"ADMIN, USER"})
                .createdAt(Instant.now())
                .isActive(true)
                .build();
    }
}
