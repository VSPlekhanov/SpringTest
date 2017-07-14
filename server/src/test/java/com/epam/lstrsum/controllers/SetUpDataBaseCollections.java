package com.epam.lstrsum.controllers;

import com.epam.lstrsum.model.Answer;
import com.epam.lstrsum.model.Request;
import com.epam.lstrsum.model.Subscription;
import com.epam.lstrsum.model.User;
import org.bson.Document;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.After;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class SetUpDataBaseCollections {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Before
    public void setUp() throws Exception {
        loadJsonResourcesAndFillDBCollectionWithThem("src/test/resources/data/userLoad.json",
                User.USER_COLLECTION_NAME);

        loadJsonResourcesAndFillDBCollectionWithThem("src/test/resources/data/requestLoad.json",
                Request.REQUEST_COLLECTION_NAME);

        loadJsonResourcesAndFillDBCollectionWithThem("src/test/resources/data/answerLoad.json",
                Answer.ANSWER_COLLECTION_NAME);

        loadJsonResourcesAndFillDBCollectionWithThem("src/test/resources/data/subscriptionLoad.json",
                Subscription.SUBSCRIPTION_COLLECTION_NAME);
    }

    @After
    public void tearDown() throws Exception {
        List<Class> collections = Arrays.asList(User.class, Request.class, Answer.class, Subscription.class);
        collections.forEach(c -> mongoTemplate.dropCollection(c.getName()));
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
