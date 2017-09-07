package com.epam.lstrsum.service;

import com.epam.lstrsum.SetUpDataBaseCollections;
import com.epam.lstrsum.aggregators.AnswerAggregator;
import com.epam.lstrsum.dto.answer.AnswerAllFieldsDto;
import com.epam.lstrsum.dto.answer.AnswerBaseDto;
import com.epam.lstrsum.dto.answer.AnswerPostDto;
import com.epam.lstrsum.exception.AnswerValidationException;
import com.epam.lstrsum.model.Answer;
import com.epam.lstrsum.model.Question;
import com.epam.lstrsum.model.QuestionWithAnswersCount;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.io.IOException;
import java.util.List;

import static com.epam.lstrsum.testutils.InstantiateUtil.EXISTING_QUESTION_ID;
import static com.epam.lstrsum.testutils.InstantiateUtil.someAnswer;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class AnswerServiceTest extends SetUpDataBaseCollections {
    private final String authorEmail = "Bob_Hoplins@epam.com";
    @Autowired
    private AnswerService answerService;
    @Autowired
    private AnswerAggregator answerAggregator;
    @Autowired
    private QuestionService questionService;
    @Autowired
    private MongoTemplate mongoTemplate;

    private static final int ANSWERS_COUNT = 3;
    private static final int PAGE_SIZE = 2;

    @Test
    public void addNewAnswerWithExistingQuestionTest() throws Exception {
        AnswerPostDto postDto = new AnswerPostDto("1u_2r", "answer text");

        AnswerAllFieldsDto answer = answerService.addNewAnswer(postDto, authorEmail);
        assertThat(answer.getQuestionId(), notNullValue());
    }

    @Test
    public void checkAnswerDtoConverterInvocation() {
        Answer someAnswer = someAnswer();
        AnswerAllFieldsDto expected = answerAggregator.modelToAllFieldsDto(someAnswer);

        assertThat(answerAggregator.modelToAllFieldsDto(someAnswer), equalTo(expected));
    }

    @Test(expected = AnswerValidationException.class)
    public void addNewAnswerNoDTOTest() throws IOException {
        answerService.addNewAnswer(null, authorEmail);
    }

    @Test(expected = AnswerValidationException.class)
    public void addNewAnswerNoAuthorTest() throws IOException {
        AnswerPostDto postDto = new AnswerPostDto("1u_2r", "answer text");
        answerService.addNewAnswer(postDto, null);
    }

    @Test(expected = AnswerValidationException.class)
    public void addNewAnswerWithEmptyText() throws IOException {
        AnswerPostDto postDto = new AnswerPostDto("1u_2r", "     ");
        answerService.addNewAnswer(postDto, authorEmail);
    }

    @Test(expected = AnswerValidationException.class)
    public void addNewAnswerWithNullParentId() throws IOException {
        AnswerPostDto postDto = new AnswerPostDto(null, "answer text");
        answerService.addNewAnswer(postDto, authorEmail);
    }

    @Test(expected = AnswerValidationException.class)
    public void addNewAnswerWithNoExistingUserTest() throws IOException {
        AnswerPostDto postDto = new AnswerPostDto("1u_2r", "answer text");
        answerService.addNewAnswer(postDto, "someone_nonexisting@epam.com");
    }

    @Test(expected = AnswerValidationException.class)
    public void addNewAnswerWithNoExistingQuestionIDTest() throws IOException {
        AnswerPostDto postDto = new AnswerPostDto("1s_2r", "answer text");
        answerService.addNewAnswer(postDto, "John_Doe@epam.com");
    }

    @Test
    public void deleteAllAnswersToQuestion() {
        final String validQuestionId =  EXISTING_QUESTION_ID;

        assertThat(answerService.getAnswersByQuestionId(validQuestionId).size()).isGreaterThan(0);

        answerService.deleteAllAnswersOnQuestion(validQuestionId);

        assertThat(answerService.getAnswersByQuestionId(validQuestionId)).hasSize(0);
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
        final int enormousPageSize = Integer.MAX_VALUE;

        assertThat(answerService.getAnswersByQuestionId(EXISTING_QUESTION_ID, 0, enormousPageSize)).hasSize(ANSWERS_COUNT);
    }

    @Test
    public void findAnswersByQuestionIdNegativePageSize(){
        final int negativePageSize = Integer.MIN_VALUE;

        assertThat(answerService.getAnswersByQuestionId(EXISTING_QUESTION_ID, 0, negativePageSize)).hasSize(ANSWERS_COUNT);
    }

    @Test
    public void findAnswersByQuestionIdZeroPageSize(){
        final int zeroPageSize = 0;

        assertThat(answerService.getAnswersByQuestionId(EXISTING_QUESTION_ID, 0, zeroPageSize)).hasSize(ANSWERS_COUNT);
    }

    @Test
    public void findAnswersByQuestionIdInCorrectAscOrder() {
        List<AnswerBaseDto> answers = answerService.getAnswersByQuestionId(EXISTING_QUESTION_ID);

        for (int i = 1; i < answers.size(); i++) {
            assertThat(answers.get(i - 1).getCreatedAt().
                    isBefore(answers.get(i).getCreatedAt()), is(true));
        }
    }

    @Test
    public void findAnswersByQuestionIdPaginationWorks() {
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
}