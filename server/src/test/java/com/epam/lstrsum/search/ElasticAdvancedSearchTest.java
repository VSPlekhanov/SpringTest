package com.epam.lstrsum.search;

import com.epam.lstrsum.controller.QuestionController;
import com.epam.lstrsum.controller.UserRuntimeRequestComponent;
import com.epam.lstrsum.model.User;
import com.epam.lstrsum.persistence.UserRepository;
import com.epam.lstrsum.service.ElasticSearchServiceTest;
import com.epam.lstrsum.testutils.AssertionUtils;
import lombok.Setter;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.entity.FileEntity;
import org.assertj.core.api.Assertions;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.elasticsearch.client.RestClient;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.SocketUtils;
import pl.allegro.tech.embeddedelasticsearch.EmbeddedElastic;
import pl.allegro.tech.embeddedelasticsearch.IndexSettings;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Optional;

import static java.util.concurrent.TimeUnit.MINUTES;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static pl.allegro.tech.embeddedelasticsearch.PopularProperties.CLUSTER_NAME;
import static pl.allegro.tech.embeddedelasticsearch.PopularProperties.HTTP_PORT;
import static pl.allegro.tech.embeddedelasticsearch.PopularProperties.TRANSPORT_TCP_PORT;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
public class ElasticAdvancedSearchTest {

    @Autowired
    private QuestionController questionController;

    @MockBean
    private UserRuntimeRequestComponent userRuntimeRequestComponent;

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private RestClient restClient;

    @Autowired
    private ObjectMapper mapper;

    private static EmbeddedElastic embeddedElastic;

    private static String data = "src/test/resources/elastic/questionLoadForElastic.json";
    private static String version = "5.6.3";
    private static String index = "experiencedatabase";
    private static String questionType ="Question";
    private static String testEsJavaOpts = "-Xms128m -Xmx512m";
    private static String host = "localhost";
    private static String clusterName = "myTestCluster";
    private static int timeoutInMinutes = 2;
    private static String questionMapping = "elastic/question-mapping.json";
    private static String analyzeSettings = "elastic/elastic-settings.json";
    private static String downloadDirectory = "target/elastic/download";
    private static Integer httpPort = SocketUtils.findAvailableTcpPort();
    private static Integer transportTcpPort = SocketUtils.findAvailableTcpPort();
    private static String uniqueInstallDirectory = String.format("%s/%d_%d",
            "target/elastic/installation", httpPort, transportTcpPort);

    @TestConfiguration
    @Setter
    static class ElasticSearchServiceTestConfiguration {

        @Bean
        @Primary
        public RestClient restClient() {
            return RestClient.builder(new HttpHost(host, httpPort, "http")).build();
        }

        @Bean
        @Primary
        public ObjectMapper mapper() {
            return new ObjectMapper();
        }

    }

    @BeforeClass
    public static void startEmbeddedElastic() throws Exception{
        InputStream questionsStream = ElasticSearchServiceTest.class.getClassLoader().getResourceAsStream(questionMapping);
        InputStream settingsStream = ElasticSearchServiceTest.class.getClassLoader().getResourceAsStream(analyzeSettings);
        embeddedElastic = EmbeddedElastic.builder()
                .withElasticVersion(version)
                .withEsJavaOpts(testEsJavaOpts)
                // distinct values for these 3 following settings for each instance
                .withSetting(HTTP_PORT, httpPort)
                .withSetting(TRANSPORT_TCP_PORT, transportTcpPort)
                .withInstallationDirectory(new File(uniqueInstallDirectory))
                // common location of downloaded files for every configuration
                .withDownloadDirectory(new File(downloadDirectory))
                .withSetting(CLUSTER_NAME, clusterName)
                .withStartTimeout(timeoutInMinutes, MINUTES)
                .withIndex(index,
                        IndexSettings.builder()
                                .withType(questionType, questionsStream)
                                .withSettings(settingsStream)
                                .build())
                .build()
                .start();
    }

    @Before
    public void setUp() throws Exception {
       loadJsonResourcesAndFillIndex(data);
       when(userRuntimeRequestComponent.isInDistributionList()).thenReturn(true);
       when(userRuntimeRequestComponent.getEmail()).thenReturn("John_Doe@epam.com");
    }

    @After
    public void tearDown() throws Exception{
        embeddedElastic.deleteIndex(index);
    }


    @AfterClass
    public static void cleanEmbeddedInstallDirectory() throws IOException{
        FileSystemUtils.deleteRecursively(new File(uniqueInstallDirectory));
        embeddedElastic.stop();

    }

    private void loadJsonResourcesAndFillIndex(String resourcesPath) throws IOException {
        embeddedElastic.recreateIndices();
        HttpEntity entity = new FileEntity(new File(resourcesPath));
        restClient.performRequest(
                "POST",
                String.format("/%s/%s/_bulk", index, questionType),
                Collections.emptyMap(),
                entity);
        embeddedElastic.refreshIndices();
    }

    @Test
    public void advancedSearchEmptyQuery() throws Exception {
        ResponseEntity<String> response = questionController.advancedSearch("", 0, 10);
        JsonNode nodeResult = mapper.readTree(response.getBody());

        Assertions.assertThat(response).satisfies(AssertionUtils::hasStatusOk);
        assertFalse(nodeResult.path("hits").isMissingNode());
        assertThat(nodeResult.path("hits").path("hits").size(), is(6));
    }

    @Test
    public void searchWordWithMetaTagsInTitle() throws Exception {
        String result = questionController.advancedSearch("title: JsonMappingException", 0, 10).getBody();
        JsonNode nodeResult = mapper.readTree(result);

        assertFalse(nodeResult.path("hits").path("hits").isMissingNode());
        assertThat(nodeResult.path("hits").path("hits").size(), is(1));
        assertThat(nodeResult.path("hits").path("hits").get(0).get("_source").path("id").getTextValue(), is("1u_1r"));
    }

    @Test
    public void searchWordsWithMetaTagsInTitle() throws Exception {
        String result = questionController.advancedSearch("title: javascript, how", 0, 10).getBody();
        JsonNode nodeResult = mapper.readTree(result);

        assertFalse(nodeResult.path("hits").path("hits").isMissingNode());
        assertThat(nodeResult.path("hits").path("hits").size(), is(1));
        assertThat(nodeResult.path("hits").path("hits").get(0).get("_source").path("id").getTextValue(), is("2u_3r"));
    }

    @Test
    public void searchPhraseWithMetaTagsInTitle() throws Exception {
        String result = questionController.advancedSearch("title: \"javascript language\"", 0, 10).getBody();
        JsonNode nodeResult = mapper.readTree(result);

        assertFalse(nodeResult.path("hits").path("hits").isMissingNode());
        assertThat(nodeResult.path("hits").path("hits").size(), is(1));
        assertThat(nodeResult.path("hits").path("hits").get(0).get("_source").path("id").getTextValue(), is("2u_3r"));
    }

    @Test
    public void searchWithMetaTagsInTagsAndTitle() throws Exception {
        String result = questionController.advancedSearch("title: javascript tags: javascript, iphone", 0, 10).getBody();
        JsonNode nodeResult = mapper.readTree(result);

        assertFalse(nodeResult.path("hits").path("hits").isMissingNode());
        assertThat(nodeResult.path("hits").path("hits").size(), is(2));
        assertThat(nodeResult.path("hits").path("hits").get(1).get("_source").path("id").getTextValue(), is("4u_5r"));
        assertThat(nodeResult.path("hits").path("hits").get(0).get("_source").path("id").getTextValue(), is("2u_3r"));
    }

    @Test
    public void searchPhraseWithMetaTagsInText() throws Exception {
        String result = questionController.advancedSearch("text: \"Mac OS\", code", 0, 10).getBody();
        JsonNode nodeResult = mapper.readTree(result);

        assertFalse(nodeResult.path("hits").path("hits").isMissingNode());
        assertThat(nodeResult.path("hits").path("hits").size(), is(1));
        assertThat(nodeResult.path("hits").path("hits").get(0).get("_source").path("id").getTextValue(), is("1u_2r"));
    }

    @Test
    public void searchPhraseWithQueryString() throws Exception {
        String result = questionController.advancedSearch("\"javascript jQuery pipe\"", 0, 10).getBody();
        JsonNode nodeResult = mapper.readTree(result);

        assertFalse(nodeResult.path("hits").path("hits").isMissingNode());
        assertThat(nodeResult.path("hits").path("hits").size(), is(4));
        assertThat(nodeResult.path("hits").path("hits").get(3).get("_source").path("id").getTextValue(), is("3u_4r"));
        assertThat(nodeResult.path("hits").path("hits").get(2).get("_source").path("id").getTextValue(), is("4u_5r"));
        assertThat(nodeResult.path("hits").path("hits").get(1).get("_source").path("id").getTextValue(), is("6u_6r"));
        assertThat(nodeResult.path("hits").path("hits").get(0).get("_source").path("id").getTextValue(), is("2u_3r"));
    }

    @Test
    public void searchCombineMetaTagsAndQueryString() throws Exception {
        String result = questionController.advancedSearch("\"tags:javascript prototype\"", 0, 10).getBody();
        JsonNode nodeResult = mapper.readTree(result);

        assertFalse(nodeResult.path("hits").path("hits").isMissingNode());
        assertThat(nodeResult.path("hits").path("hits").size(), is(1));
        assertThat(nodeResult.path("hits").path("hits").get(0).get("_source").path("id").getTextValue(), is("4u_5r"));
    }

    @Test
    public void searchPaging() throws Exception {
        String result = questionController.advancedSearch("", 1, 2).getBody();
        JsonNode nodeResult = mapper.readTree(result);

        assertFalse(nodeResult.path("hits").isMissingNode());
        assertThat(nodeResult.path("hits").path("hits").size(), is(2));
        assertThat(nodeResult.path("hits").path("hits").get(0).get("_source").path("id").getTextValue(), is("1u_2r"));
        assertThat(nodeResult.path("hits").path("hits").get(1).get("_source").path("id").getTextValue(), is("6u_6r"));
    }

    @Test
    public void searchNoExistWords() throws Exception {
        String result =  questionController.advancedSearch("title: Scala", 0, 10).getBody();
        JsonNode nodeResult = mapper.readTree(result);

        assertFalse(nodeResult.path("hits").isMissingNode());
        assertThat(nodeResult.path("hits").path("hits").size(), is(0));
    }

    @Test
    public void getBadResponseFromParser() throws IOException {
        String result = questionController.advancedSearch("errorMetatag: Scala Lisp", 0, 10).getBody();
        JsonNode nodeResult = mapper.readTree(result);

        assertThat(nodeResult.get("indexes").get(0).path("indexFrom").getIntValue(), is(0));
        assertThat(nodeResult.get("indexes").get(0).path("indexTo").getIntValue(), is(20));
    }

    @Test
    public void searchWordWithQueryString() throws Exception {
        String result = questionController.advancedSearch("code", 0, 10).getBody();
        JsonNode nodeResult = mapper.readTree(result);

        assertFalse(nodeResult.path("hits").path("hits").isMissingNode());
        assertThat(nodeResult.path("hits").path("hits").size(), is(2));
        assertThat(nodeResult.path("hits").path("hits").get(1).get("_source").path("id").getTextValue(), is("6u_6r"));
        assertThat(nodeResult.path("hits").path("hits").get(0).get("_source").path("id").getTextValue(), is("1u_2r"));
    }


    @Test
    public void searchWithMetaTagsInTags() throws Exception {
        String result = questionController.advancedSearch("tags: java, android title: how", 0, 10).getBody();
        JsonNode nodeResult = mapper.readTree(result);

        assertFalse(nodeResult.path("hits").path("hits").isMissingNode());
        assertThat(nodeResult.path("hits").path("hits").size(), is(3));
        assertThat(nodeResult.path("hits").path("hits").get(2).get("_source").path("id").getTextValue(), is("3u_4r"));
        assertThat(nodeResult.path("hits").path("hits").get(1).get("_source").path("id").getTextValue(), is("1u_1r"));
        assertThat(nodeResult.path("hits").path("hits").get(0).get("_source").path("id").getTextValue(), is("2u_3r"));
    }

    private void prepareUserOutsideTheDL(String currentUserId) {
        User currentUser = new User();
        currentUser.setUserId(currentUserId);
        when(userRuntimeRequestComponent.isInDistributionList()).thenReturn(false);
        when(userRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.of(currentUser));
    }

    @Test
    public void currentUserIsNotInDistributionList() throws Exception {
        String currentUserId = "currentUserIsOutsideTheDL";
        prepareUserOutsideTheDL(currentUserId);

        String result = questionController.advancedSearch("\"javascript jQuery pipe\"", 0, 10).getBody();
        JsonNode nodeResult = mapper.readTree(result);

        JsonNode hits = nodeResult.path("hits").path("hits");
        assertFalse(hits.isMissingNode());
        assertThat(hits.size(), is(0));
    }

    private String getUserIdAsDBRef(String currentUserId) {
        return String.format("DBRef(u'User', ObjectId('%s'))", currentUserId);
    }

    @Test
    public void userNotInDLButIsTheAuthor() throws Exception {
        String currentUserId = "1u";
        String userIdAsDBRef = getUserIdAsDBRef(currentUserId);
        prepareUserOutsideTheDL(currentUserId);

        String result = questionController.advancedSearch("title: automation", 0, 10).getBody();
        JsonNode nodeResult = mapper.readTree(result);

        JsonNode hits = nodeResult.path("hits").path("hits");
        assertFalse(hits.isMissingNode());
        assertThat(hits.size(), is(1));

        JsonNode foundQuestion = hits.get(0).get("_source");
        assertThat(foundQuestion.path("authorId").getTextValue(), is(userIdAsDBRef));

        JsonNode allowedSubs = foundQuestion.path("allowedSubs");
        allowedSubs.forEach( sub -> assertNotEquals(userIdAsDBRef, sub.getTextValue()) );
    }

    @Test
    public void userNotInDLButIsInAllowedSubs() throws Exception {
        String currentUserId = "1u";
        String userIdAsDBRef = getUserIdAsDBRef(currentUserId);
        prepareUserOutsideTheDL(currentUserId);

        String result = questionController.advancedSearch("title: how", 0, 10).getBody();
        JsonNode nodeResult = mapper.readTree(result);

        JsonNode hits = nodeResult.path("hits").path("hits");
        assertFalse(hits.isMissingNode());
        assertThat(hits.size(), is(2));

        assertNotEquals(userIdAsDBRef, hits.get(0).get("_source").path("authorId").getTextValue());
        assertNotEquals(userIdAsDBRef, hits.get(1).get("_source").path("authorId").getTextValue());

        assertThat(hits.get(0).get("_source").path("allowedSubs").get(0).getTextValue(), is(userIdAsDBRef));
        assertThat(hits.get(1).get("_source").path("allowedSubs").get(0).getTextValue(), is(userIdAsDBRef));
    }

    @Test
    public void userNotInDLButIsTheAuthorOrInAllowedSubs() throws Exception {
        String currentUserId = "1u";
        String userIdAsDBRef = getUserIdAsDBRef(currentUserId);
        prepareUserOutsideTheDL(currentUserId);

        String result = questionController.advancedSearch("title: how tags: eclipse", 0, 10).getBody();
        JsonNode nodeResult = mapper.readTree(result);

        JsonNode hits = nodeResult.path("hits").path("hits");
        assertFalse(hits.isMissingNode());
        assertThat(hits.size(), is(3));

        hits.forEach( hit -> {
            JsonNode question = hit.get("_source");
            boolean isTheAuthor = userIdAsDBRef.equals(question.path("authorId").getTextValue());
            boolean isInAllowedSubs = false;
            JsonNode allowedSubs = question.path("allowedSubs");
            for (JsonNode sub : allowedSubs) {
                if (userIdAsDBRef.equals(sub.getTextValue())) {
                    isInAllowedSubs = true;
                    break;
                }
            }
            assertTrue(isTheAuthor || isInAllowedSubs);
        });
    }
}
