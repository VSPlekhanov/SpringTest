package com.epam.lstrsum;

import com.epam.lstrsum.model.Answer;
import com.epam.lstrsum.model.Question;
import com.epam.lstrsum.model.Subscription;
import com.epam.lstrsum.model.User;
import com.epam.lstrsum.model.Vote;
import org.bson.Document;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ActiveProfiles("unsecured")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
public abstract class SetUpDataBaseCollections {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Before
    public void setUp() throws Exception {
        loadJsonResourcesAndFillDBCollectionWithThem("src/test/resources/data/userLoad.json",
                User.USER_COLLECTION_NAME);

        loadJsonResourcesAndFillDBCollectionWithThem("src/test/resources/data/questionLoad.json",
                Question.QUESTION_COLLECTION_NAME);

        loadJsonResourcesAndFillDBCollectionWithThem("src/test/resources/data/answerLoad.json",
                Answer.ANSWER_COLLECTION_NAME);

        loadJsonResourcesAndFillDBCollectionWithThem("src/test/resources/data/subscriptionLoad.json",
                Subscription.SUBSCRIPTION_COLLECTION_NAME);

        loadJsonResourcesAndFillDBCollectionWithThem("src/test/resources/data/voteLoad.json",
                Vote.VOTE_COLLECTION_NAME);
    }

    @After
    public void tearDown() throws Exception {
        List<Class> collections = Arrays.asList(User.class, Question.class, Answer.class, Subscription.class, Vote.class);
        collections.forEach(c -> mongoTemplate.remove(new Query(), c.getSimpleName()));
    }

    private void loadJsonResourcesAndFillDBCollectionWithThem(String resourcesPath, String collectionName) throws IOException, ParseException {
        List<String> jsonResources = loadJsonResources(resourcesPath);

        fillDataBaseCollectionWith(jsonResources, collectionName);
    }

    private void fillDataBaseCollectionWith(List<String> filling, String collectionName) {

        for (String element : filling) {
            Document dbDocument = Document.parse(element);
            mongoTemplate.save(dbDocument, collectionName);
        }
    }

    private List<String> loadJsonResources(String resourcesPath) throws IOException, ParseException {
        JSONParser parser = new JSONParser();
        JSONArray jsonArray = (JSONArray) parser.parse(new FileReader(resourcesPath));
        List<String> jsonList = new ArrayList<>();

        for (Object ans : jsonArray) {
            jsonList.add(ans.toString());
        }

        return jsonList;
    }
}
