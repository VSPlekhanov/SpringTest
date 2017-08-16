package com.epam.lstrsum.service;

import com.epam.lstrsum.SetUpDataBaseCollections;
import com.epam.lstrsum.aggregators.AnswerAggregator;
import com.epam.lstrsum.converter.AnswerDtoMapper;
import com.epam.lstrsum.dto.answer.AnswerAllFieldsDto;
import com.epam.lstrsum.dto.answer.AnswerPostDto;
import com.epam.lstrsum.exception.AnswerValidationException;
import com.epam.lstrsum.model.Answer;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

import static com.epam.lstrsum.testutils.InstantiateUtil.someAnswer;
import static org.assertj.core.api.Assertions.assertThat;
import static com.epam.lstrsum.testutils.InstantiateUtil.someAnswer;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class AnswerServiceTest extends SetUpDataBaseCollections {
    @Autowired
    private AnswerService answerService;

    @Autowired
    private AnswerAggregator answerAggregator;

    @Autowired
    private QuestionService questionService;

    private final String authorEmail = "Bob_Hoplins@epam.com";

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
        final String validQuestionId = "1u_1r";

        assertThat(questionService.getQuestionAppearanceDotByQuestionId(validQuestionId).getAnswers().size()).isGreaterThan(0);

        answerService.deleteAllAnswersOnQuestion(validQuestionId);

        assertThat(questionService.getQuestionAppearanceDotByQuestionId(validQuestionId).getAnswers()).hasSize(0);
    }
}