package com.epam.lstrsum.service;

import com.epam.lstrsum.SetUpDataBaseCollections;
import com.epam.lstrsum.aggregators.AnswerAggregator;
import com.epam.lstrsum.dto.answer.AnswerAllFieldsDto;
import com.epam.lstrsum.dto.answer.AnswerBaseDto;
import com.epam.lstrsum.dto.answer.AnswerPostDto;
import com.epam.lstrsum.exception.BusinessLogicException;
import com.epam.lstrsum.exception.NoSuchAnswerException;
import com.epam.lstrsum.exception.QuestionValidationException;
import com.epam.lstrsum.model.Answer;
import com.epam.lstrsum.model.Question;
import com.epam.lstrsum.model.QuestionWithAnswersCount;
import lombok.val;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.io.IOException;
import java.util.stream.Collectors;

import static com.epam.lstrsum.testutils.InstantiateUtil.EXISTING_ANSWER_ID;
import static com.epam.lstrsum.testutils.InstantiateUtil.EXISTING_QUESTION_ID;
import static com.epam.lstrsum.testutils.InstantiateUtil.EXISTING_USER_EMAIL;
import static com.epam.lstrsum.testutils.InstantiateUtil.SOME_USER_EMAIL;
import static com.epam.lstrsum.testutils.InstantiateUtil.someAnswer;
import static com.epam.lstrsum.testutils.InstantiateUtil.someAnswerPostDto;
import static com.epam.lstrsum.testutils.InstantiateUtil.someAnswerPostDtoWithQuestionId;
import static com.epam.lstrsum.testutils.InstantiateUtil.someString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class AnswerServiceTest extends SetUpDataBaseCollections {
    private static final int ANSWERS_COUNT = 3;
    private static final int PAGE_SIZE = 2;
    private final String authorEmail = "Bob_Hoplins@epam.com";
    @Autowired
    private AnswerService answerService;
    @Autowired
    private AnswerAggregator answerAggregator;
    @Autowired
    private MongoTemplate mongoTemplate;

    @Test
    public void addNewAnswerWithExistingQuestionTest() throws Exception {
        AnswerPostDto postDto = new AnswerPostDto("1u_2r", someString());

        AnswerAllFieldsDto answer = answerService.addNewAnswer(postDto, authorEmail);
        assertThat(answer.getQuestionId(), notNullValue());
    }

    @Test
    public void addNewAnswerWithAllowedSubAndExistingQuestionTest() throws Exception {
        AnswerPostDto postDto = new AnswerPostDto(EXISTING_QUESTION_ID, someString());
        String allowedSubEmail = EXISTING_USER_EMAIL;

        AnswerAllFieldsDto answer = answerService.addNewAnswerWithAllowedSub(postDto, allowedSubEmail);
        assertThat(answer.getQuestionId(), notNullValue());
    }

    @Test(expected = QuestionValidationException.class)
    public void addNewAnswerWithAllowedSubAndNonExistingQuestionTest() throws Exception {
        answerService.addNewAnswerWithAllowedSub(someAnswerPostDto(), someString());
    }

    @Test(expected = BusinessLogicException.class)
    public void addNewAnswerWithNotAllowedSubUser() throws Exception {
        answerService.addNewAnswerWithAllowedSub(someAnswerPostDtoWithQuestionId(EXISTING_QUESTION_ID), someString());
    }

    @Test
    public void checkAnswerDtoConverterInvocation() {
        Answer someAnswer = someAnswer();
        AnswerAllFieldsDto expected = answerAggregator.modelToAllFieldsDto(someAnswer);

        assertThat(answerAggregator.modelToAllFieldsDto(someAnswer), equalTo(expected));
    }

    @Test(expected = QuestionValidationException.class)
    public void addNewAnswerWithNoExistingQuestionIDTest() throws IOException {
        answerService.addNewAnswer(someAnswerPostDtoWithQuestionId(someString()), SOME_USER_EMAIL);
    }

    @Test
    public void deleteAllAnswersToQuestion() {
        assertThat(answerService.getAnswersByQuestionId(EXISTING_QUESTION_ID).size()).isGreaterThan(0);

        answerService.deleteAllAnswersOnQuestion(EXISTING_QUESTION_ID);

        assertThat(answerService.getAnswersByQuestionId(EXISTING_QUESTION_ID)).hasSize(0);
    }

    @Test
    public void aggregationFunctionTestingShouldReturnQuestionToAnswersCount() {
        assertThat(answerService.aggregateToCount(mongoTemplate.findAll(Question.class)))
                .anySatisfy(q -> hasQuestionWithAnswersCount(q, "1u_1r", 3))
                .anySatisfy(q -> hasQuestionWithAnswersCount(q, "1u_2r", 2))
                .anySatisfy(q -> hasQuestionWithAnswersCount(q, "2u_3r", 2))
                .anySatisfy(q -> hasQuestionWithAnswersCount(q, "3u_4r", 2))
                .anySatisfy(q -> hasQuestionWithAnswersCount(q, "4u_5r", 3))
                .anySatisfy(q -> hasQuestionWithAnswersCount(q, "6u_6r", 0));
    }

    @Test
    public void findAnswersByQuestionIdBigPageSize() {
        val enormousPageSize = Integer.MAX_VALUE;

        assertThat(answerService.getAnswersByQuestionId(EXISTING_QUESTION_ID, 0, enormousPageSize)).hasSize(ANSWERS_COUNT);
    }

    @Test
    public void findAnswersByQuestionIdNegativePageSize() {
        val negativePageSize = Integer.MIN_VALUE;

        assertThat(answerService.getAnswersByQuestionId(EXISTING_QUESTION_ID, 0, negativePageSize)).hasSize(ANSWERS_COUNT);
    }

    @Test
    public void findAnswersByQuestionIdZeroPageSize() {
        val zeroPageSize = 0;

        assertThat(answerService.getAnswersByQuestionId(EXISTING_QUESTION_ID, 0, zeroPageSize)).hasSize(ANSWERS_COUNT);
    }

    @Test
    public void findAnswersByQuestionIdInCorrectAscOrder() {
        val answers = answerService.getAnswersByQuestionId(EXISTING_QUESTION_ID);

        assertThat(
                answers.stream()
                        .map(AnswerBaseDto::getCreatedAt)
                        .collect(Collectors.toList())
        ).isSorted();
    }

    @Test
    public void findAnswersByQuestionIdPaginationWorks() {
        assertThat(answerService.getAnswersByQuestionId(EXISTING_QUESTION_ID, -1, PAGE_SIZE)).hasSize(2);
        assertThat(answerService.getAnswersByQuestionId(EXISTING_QUESTION_ID, 0, PAGE_SIZE)).hasSize(2);
        assertThat(answerService.getAnswersByQuestionId(EXISTING_QUESTION_ID, 1, PAGE_SIZE)).hasSize(1);
        assertThat(answerService.getAnswersByQuestionId(EXISTING_QUESTION_ID, 2, PAGE_SIZE)).isEmpty();
    }

    private void hasQuestionWithAnswersCount(
            QuestionWithAnswersCount source, String questionId, int answersCount
    ) {
        assertThat(source.getQuestionId().getQuestionId()).isEqualTo(questionId);
        assertThat(source.getCount()).isEqualTo(answersCount);
    }

    @Test
    public void findAnswerByAnswerIdIsNotNull() {
        val answer = answerService.getAnswerById(EXISTING_ANSWER_ID);
        assertThat(answer).isNotNull();
    }

    @Test(expected = NoSuchAnswerException.class)
    public void findAnswerByAnswerIdForNotExistingAnswerId() {
        answerService.getAnswerById(someAnswer().getAnswerId());
    }

    @Test
    public void saveAnswer() {
        val newAnswerText = someString();
        val answerSaved = answerService.getAnswerById(EXISTING_ANSWER_ID);
        answerSaved.setText(newAnswerText);
        answerService.save(answerSaved);
        val answerLoaded = answerService.getAnswerById(EXISTING_ANSWER_ID);

        assertThat(answerLoaded.getText()).isEqualTo(newAnswerText);
    }
}