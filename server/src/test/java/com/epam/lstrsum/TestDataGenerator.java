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
    private static List<Question> questions = new ArrayList<>(1000);
    private static List<Answer> answers = new ArrayList<>(1000);
    private static List<Subscription> subscriptions = new ArrayList<>(1000);

    @BeforeClass
    public static void generateCollectionLists() {
        for (int i = 0; i < 1000; i++) {
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
        assertThat(answers.size(), is(1000));
        final DBObject dbObject = new BasicDBList();
        mongoTemplate.getConverter().write(answers, dbObject);
        final String dbAnswers = dbObject.toString().replace("_class\" : \"" + Answer.class.getCanonicalName() + "\" ,", "");
        final String dbAnswers2 = dbAnswers.replace(", \"_class\" : \"" + Answer.class.getCanonicalName() + "\"", "");
        Files.write(Paths.get("src/test/resources/data/generated/Answers.json"), dbAnswers2.getBytes());
    }

    @Ignore
    @Test
    public void generateQuestionCollectionTest() throws IOException {
        assertThat(questions.size(), is(1000));
        final DBObject dbObject = new BasicDBList();
        mongoTemplate.getConverter().write(questions, dbObject);
        final String dbQuestions = dbObject.toString().replace("_class\" : \"" + Question.class.getCanonicalName() + "\" ,", "");
        final String dbQuestion2 = dbQuestions.replace(", \"_class\" : \"" + Question.class.getCanonicalName() + "\"", "");
        Files.write(Paths.get("src/test/resources/data/generated/Questions.json"), dbQuestion2.getBytes());
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


    private static Answer buildAnswer(int numberOfAnswer, Question question) {
        int rndId = ThreadLocalRandom.current().nextInt(question.getAllowedSubs().size());
        return Answer.builder()
                .answerId("AnswerId" + numberOfAnswer)
                .parentId(question)
                .text("Some interesting answer for some interesting question" + numberOfAnswer)
                .createdAt(Instant.now())
                .authorId(question.getAllowedSubs().get(rndId))
                .upVote(ThreadLocalRandom.current().nextInt(15))
                .build();
    }

    private static Subscription buildSubscription(int numberOfSubscription, User user, List<Question> questions) {
        final Subscription subscription = new Subscription();
        subscription.setSubscriptionId("SubscriptionId" + numberOfSubscription);
        subscription.setUserId(user);
        final List<Question> subQuestions = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            final Question question = questions.get(new Random().nextInt(1000));
            if (!subQuestions.contains(question)) {
                subQuestions.add(question);
            }
        }
        subscription.setQuestionIds(subQuestions);
        return subscription;
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
