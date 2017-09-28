package com.epam.lstrsum.search;

import com.epam.lstrsum.controller.QuestionController;
import com.epam.lstrsum.dto.question.QuestionAdvancedSearchResultDto;
import com.epam.lstrsum.service.ElasticSearchServiceTest;
import com.epam.lstrsum.testutils.AssertionUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.entity.FileEntity;
import org.assertj.core.api.Assertions;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.elasticsearch.client.RestClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.SocketUtils;
import pl.allegro.tech.embeddedelasticsearch.EmbeddedElastic;
import pl.allegro.tech.embeddedelasticsearch.IndexSettings;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

import static java.util.concurrent.TimeUnit.MINUTES;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static pl.allegro.tech.embeddedelasticsearch.PopularProperties.CLUSTER_NAME;
import static pl.allegro.tech.embeddedelasticsearch.PopularProperties.HTTP_PORT;
import static pl.allegro.tech.embeddedelasticsearch.PopularProperties.TRANSPORT_TCP_PORT;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
public class ElasticAdvancedSearchTest {

    @Autowired
    private QuestionController questionController;

    private static String EXPERIENCE_INDEX_NAME = "experiencedatabase";
    private static String QUESTION_TYPE_NAME = "Question";
    private String ELASTIC_DATA_URL = "src/test/resources/elastic/questionLoadForElastic.json";

    @Autowired
    private RestClient restClient;

    @Autowired
    private EmbeddedElastic embeddedElastic;

    @Autowired
    private ObjectMapper mapper;

    @Profile("test")
    @TestConfiguration
    static class ElasticSearchServiceTestConfiguration {

        // todo to properties
        private String CLUSTER_NAME_VALUE = "myTestCluster";
        private Integer HTTP_PORT_VALUE = SocketUtils.findAvailableTcpPort();
        private String HOST = "localhost";
        private Integer TRANSPORT_TCP_PORT_VALUE = SocketUtils.findAvailableTcpPort();
        private String QUESTION_MAPPING_URL = "elastic/question-mapping.json";
        private String ELASTIC_SETTINGS_URL = "elastic/elastic-settings.json";
        private int START_TIMEOUT_IN_MINUTES = 1;
        private String TEST_ES_JAVA_OPTS = "-Xms128m -Xmx512m";
        private String ELASTIC_VERSION = "5.6.3";


        @Bean
        @Primary
        public RestClient restClient() {
            return RestClient.builder(new HttpHost(HOST, HTTP_PORT_VALUE, "http")).build();
        }

        @Bean
        @Primary
        public ObjectMapper mapper() {
            return new ObjectMapper();
        }

        @Bean(destroyMethod = "stop")
        @Primary
        public EmbeddedElastic embeddedElastic() throws Exception{
            InputStream questionsStream = ElasticSearchServiceTest.class.getClassLoader().getResourceAsStream(QUESTION_MAPPING_URL);
            InputStream settingsStream = ElasticSearchServiceTest.class.getClassLoader().getResourceAsStream(ELASTIC_SETTINGS_URL);
            return EmbeddedElastic.builder()
                    .withElasticVersion(ELASTIC_VERSION)
                    .withEsJavaOpts(TEST_ES_JAVA_OPTS)
                    .withSetting(HTTP_PORT, HTTP_PORT_VALUE)
                    .withSetting(TRANSPORT_TCP_PORT, TRANSPORT_TCP_PORT_VALUE)
                    .withSetting(CLUSTER_NAME, CLUSTER_NAME_VALUE)
                    .withPlugin("analysis-stempel")
                    .withStartTimeout(START_TIMEOUT_IN_MINUTES, MINUTES)
                    .withIndex(EXPERIENCE_INDEX_NAME,
                            IndexSettings.builder()
                                    .withType(QUESTION_TYPE_NAME, questionsStream)
                                    .withSettings(settingsStream)
                                    .build())
                    .build()
                    .start();
        }
    }

    @Before
    public void setUp() throws Exception {
       loadJsonResourcesAndFillIndex(ELASTIC_DATA_URL);
    }

    @After
    public void tearDown() throws Exception{
        embeddedElastic.deleteIndex(EXPERIENCE_INDEX_NAME);
    }

    private void loadJsonResourcesAndFillIndex(String resourcesPath) throws IOException {
        embeddedElastic.recreateIndices();
        HttpEntity entity = new FileEntity(new File(resourcesPath));
        restClient.performRequest(
                "POST",
                String.format("/%s/%s/_bulk", EXPERIENCE_INDEX_NAME, QUESTION_TYPE_NAME),
                Collections.emptyMap(),
                entity);
        embeddedElastic.refreshIndices();
    }

    @Test
    public void advancedSearchEmptyQuery() throws Exception {
        ResponseEntity<QuestionAdvancedSearchResultDto> response = questionController.advancedSearch("", 0, 10);
        QuestionAdvancedSearchResultDto resultDto =  response.getBody();
        JsonNode nodeResult = mapper.readTree(resultDto.getValue());

        Assertions.assertThat(response).satisfies(AssertionUtils::hasStatusOk);
        assertNull(resultDto.getErrorMessage());
        assertFalse(nodeResult.path("hits").isMissingNode());
        assertThat(nodeResult.path("hits").path("hits").size(), is(6));
    }

    @Test
    public void searchWordWithMetaTagsInTitle() throws Exception {
        QuestionAdvancedSearchResultDto resultDto = questionController.advancedSearch("title: JsonMappingException", 0, 10).getBody();
        JsonNode nodeResult = mapper.readTree(resultDto.getValue());

        assertNull(resultDto.getErrorMessage());
        assertFalse(nodeResult.path("hits").path("hits").isMissingNode());
        assertThat(nodeResult.path("hits").path("hits").size(), is(1));
        assertThat(nodeResult.path("hits").path("hits").get(0).get("_source").path("id").getTextValue(), is("1u_1r"));
    }

    @Test
    public void searchWordsWithMetaTagsInTitle() throws Exception {
        QuestionAdvancedSearchResultDto resultDto = questionController.advancedSearch("title: javascript, how", 0, 10).getBody();
        JsonNode nodeResult = mapper.readTree(resultDto.getValue());

        assertNull(resultDto.getErrorMessage());
        assertFalse(nodeResult.path("hits").path("hits").isMissingNode());
        assertThat(nodeResult.path("hits").path("hits").size(), is(1));
        assertThat(nodeResult.path("hits").path("hits").get(0).get("_source").path("id").getTextValue(), is("2u_3r"));
    }

    @Test
    public void searchPhraseWithMetaTagsInTitle() throws Exception {
        QuestionAdvancedSearchResultDto resultDto = questionController.advancedSearch("title: \"javascript language\"", 0, 10).getBody();
        JsonNode nodeResult = mapper.readTree(resultDto.getValue());

        assertNull(resultDto.getErrorMessage());
        assertFalse(nodeResult.path("hits").path("hits").isMissingNode());
        assertThat(nodeResult.path("hits").path("hits").size(), is(1));
        assertThat(nodeResult.path("hits").path("hits").get(0).get("_source").path("id").getTextValue(), is("2u_3r"));
    }

    @Test
    public void searchWithMetaTagsInTagsAndTitle() throws Exception {
        QuestionAdvancedSearchResultDto resultDto = questionController.advancedSearch("title: javascript tags: javascript, iphone", 0, 10).getBody();
        JsonNode nodeResult = mapper.readTree(resultDto.getValue());

        assertNull(resultDto.getErrorMessage());
        assertFalse(nodeResult.path("hits").path("hits").isMissingNode());
        assertThat(nodeResult.path("hits").path("hits").size(), is(2));
        assertThat(nodeResult.path("hits").path("hits").get(0).get("_source").path("id").getTextValue(), is("2u_3r"));
        assertThat(nodeResult.path("hits").path("hits").get(1).get("_source").path("id").getTextValue(), is("4u_5r"));
    }

    @Test
    public void searchPhraseWithMetaTagsInText() throws Exception {
        QuestionAdvancedSearchResultDto resultDto = questionController.advancedSearch("text: \"Mac OS\", code", 0, 10).getBody();
        JsonNode nodeResult = mapper.readTree(resultDto.getValue());

        assertNull(resultDto.getErrorMessage());
        assertFalse(nodeResult.path("hits").path("hits").isMissingNode());
        assertThat(nodeResult.path("hits").path("hits").size(), is(1));
        assertThat(nodeResult.path("hits").path("hits").get(0).get("_source").path("id").getTextValue(), is("1u_2r"));
    }

    @Test
    public void searchPhraseWithQueryString() throws Exception {
        QuestionAdvancedSearchResultDto resultDto = questionController.advancedSearch("\"javascript jQuery pipe\"", 0, 10).getBody();
        JsonNode nodeResult = mapper.readTree(resultDto.getValue());

        assertNull(resultDto.getErrorMessage());
        assertFalse(nodeResult.path("hits").path("hits").isMissingNode());
        assertThat(nodeResult.path("hits").path("hits").size(), is(4));
        assertThat(nodeResult.path("hits").path("hits").get(0).get("_source").path("id").getTextValue(), is("2u_3r"));
        assertThat(nodeResult.path("hits").path("hits").get(1).get("_source").path("id").getTextValue(), is("3u_4r"));
        assertThat(nodeResult.path("hits").path("hits").get(2).get("_source").path("id").getTextValue(), is("4u_5r"));
        assertThat(nodeResult.path("hits").path("hits").get(3).get("_source").path("id").getTextValue(), is("6u_6r"));
    }

    @Test
    public void searchCombineMetaTagsAndQueryString() throws Exception {
        QuestionAdvancedSearchResultDto resultDto = questionController.advancedSearch("\"tags:javascript prototype\"", 0, 10).getBody();
        JsonNode nodeResult = mapper.readTree(resultDto.getValue());

        assertNull(resultDto.getErrorMessage());
        assertFalse(nodeResult.path("hits").path("hits").isMissingNode());
        assertThat(nodeResult.path("hits").path("hits").size(), is(1));
        assertThat(nodeResult.path("hits").path("hits").get(0).get("_source").path("id").getTextValue(), is("4u_5r"));
    }

    @Test
    public void searchPaging() throws Exception {
        QuestionAdvancedSearchResultDto resultDto =  questionController.advancedSearch("", 1, 2).getBody();
        JsonNode nodeResult = mapper.readTree(resultDto.getValue());

        assertNull(resultDto.getErrorMessage());
        assertFalse(nodeResult.path("hits").isMissingNode());
        assertThat(nodeResult.path("hits").path("hits").size(), is(2));
        assertThat(nodeResult.path("hits").path("hits").get(0).get("_source").path("id").getTextValue(), is("1u_2r"));
        assertThat(nodeResult.path("hits").path("hits").get(1).get("_source").path("id").getTextValue(), is("2u_3r"));
    }

    @Test
    public void searchNoExistWords() throws Exception {
        QuestionAdvancedSearchResultDto resultDto =  questionController.advancedSearch("title: Scala", 0, 10).getBody();
        JsonNode nodeResult = mapper.readTree(resultDto.getValue());

        assertNull(resultDto.getErrorMessage());
        assertFalse(nodeResult.path("hits").isMissingNode());
        assertThat(nodeResult.path("hits").path("hits").size(), is(0));
    }

    @Test
    public void getBadResponseFromParser() throws IOException {
        QuestionAdvancedSearchResultDto resultDto = questionController.advancedSearch("errorMetatag: Scala Lisp", 0, 10).getBody();
        JsonNode nodeResult = mapper.readTree(resultDto.getValue());

        assertThat(resultDto.getErrorMessage(), is("Error in query parsing."));
        assertThat(nodeResult.get(0).path("indexFrom").getIntValue(), is(0));
        assertThat(nodeResult.get(0).path("indexTo").getIntValue(), is(20));
    }

    @Test
    public void searchWordWithQueryString() throws Exception {
        QuestionAdvancedSearchResultDto resultDto = questionController.advancedSearch("code", 0, 10).getBody();
        JsonNode nodeResult = mapper.readTree(resultDto.getValue());

        assertNull(resultDto.getErrorMessage());
        assertFalse(nodeResult.path("hits").path("hits").isMissingNode());
        assertThat(nodeResult.path("hits").path("hits").size(), is(2));
        assertThat(nodeResult.path("hits").path("hits").get(0).get("_source").path("id").getTextValue(), is("1u_2r"));
        assertThat(nodeResult.path("hits").path("hits").get(1).get("_source").path("id").getTextValue(), is("6u_6r"));
    }


    @Test
    public void searchWithMetaTagsInTags() throws Exception {
        QuestionAdvancedSearchResultDto resultDto = questionController.advancedSearch("tags: java, android title: how", 0, 10).getBody();
        JsonNode nodeResult = mapper.readTree(resultDto.getValue());

        assertNull(resultDto.getErrorMessage());
        assertFalse(nodeResult.path("hits").path("hits").isMissingNode());
        assertThat(nodeResult.path("hits").path("hits").size(), is(3));
        assertThat(nodeResult.path("hits").path("hits").get(0).get("_source").path("id").getTextValue(), is("1u_1r"));
        assertThat(nodeResult.path("hits").path("hits").get(1).get("_source").path("id").getTextValue(), is("2u_3r"));
        assertThat(nodeResult.path("hits").path("hits").get(2).get("_source").path("id").getTextValue(), is("3u_4r"));
    }


}
