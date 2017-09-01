package com.epam.lstrsum.converter;

import com.epam.lstrsum.SetUpDataBaseCollections;
import com.epam.lstrsum.dto.answer.AnswerBaseDto;
import com.epam.lstrsum.dto.answer.AnswerPostDto;
import com.epam.lstrsum.dto.question.QuestionBaseDto;
import com.epam.lstrsum.dto.user.UserBaseDto;
import com.epam.lstrsum.model.Answer;
import com.epam.lstrsum.model.Question;
import com.epam.lstrsum.model.User;
import com.epam.lstrsum.testutils.InstantiateUtil;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static com.epam.lstrsum.testutils.InstantiateUtil.someAnswer;
import static com.epam.lstrsum.testutils.InstantiateUtil.someAnswerPostDto;
import static com.epam.lstrsum.testutils.InstantiateUtil.someQuestion;
import static com.epam.lstrsum.testutils.InstantiateUtil.someQuestionBaseDto;
import static com.epam.lstrsum.testutils.InstantiateUtil.someUser;
import static com.epam.lstrsum.testutils.InstantiateUtil.someUserBaseDto;
import static com.epam.lstrsum.utils.FunctionalUtil.getListWithSize;
import static org.assertj.core.api.Assertions.assertThat;


public class AnswerDtoMapperTest extends SetUpDataBaseCollections {
    @Autowired
    private AnswerDtoMapper answerMapper;

    public static void checkAnswerBaseDto(AnswerBaseDto answerBaseDto, Answer answer, UserBaseDto userBaseDto) {
        assertThat(answerBaseDto.getAnswerId()).isEqualTo(answer.getAnswerId());
        assertThat(answerBaseDto.getCreatedAt()).isEqualTo(answer.getCreatedAt());
        assertThat(answerBaseDto.getText()).isEqualTo(answer.getText());
        assertThat(answerBaseDto.getUpVote()).isEqualTo(answer.getVotes().size());
        assertThat(answerBaseDto.getAuthorId()).isEqualTo(userBaseDto);
    }

    @Test
    public void modelToAllFieldsDto() throws Exception {
        Answer answer = someAnswer();
        UserBaseDto userBaseDto = someUserBaseDto();
        QuestionBaseDto questionBaseDto = someQuestionBaseDto();

        assertThat(answerMapper.modelToAllFieldsDto(answer, userBaseDto, questionBaseDto))
                .satisfies(a -> {
                    checkAnswerBaseDto(a, answer, userBaseDto);
                    assertThat(a.getAnswerId()).isEqualTo(answer.getAnswerId());
                    assertThat(a.getQuestionId()).isEqualTo(questionBaseDto);
                });
    }

    @Test
    public void modelToBaseDto() throws Exception {
        Answer answer = someAnswer();
        UserBaseDto userBaseDto = someUserBaseDto();

        assertThat(answerMapper.modelToBaseDto(answer, userBaseDto))
                .satisfies(a -> checkAnswerBaseDto(a, answer, userBaseDto));
    }

    @Test
    public void answersToQuestionInAnswerBaseDto() throws Exception {
        final int size = 2;
        List<Answer> answers = getListWithSize(InstantiateUtil::someAnswer, size);
        List<UserBaseDto> authors = getListWithSize(InstantiateUtil::someUserBaseDto, size);

        assertThat(answerMapper.answersToQuestionInAnswerBaseDto(answers, authors))
                .hasSize(size);
    }

    @Test
    public void answerPostDtoAndAuthorEmailToAnswer() throws Exception {
        AnswerPostDto answerPostDto = someAnswerPostDto();
        User user = someUser();
        Question question = someQuestion();

        assertThat(answerMapper.answerPostDtoAndAuthorEmailToAnswer(answerPostDto, user, question))
                .satisfies(a -> {
                    assertThat(a.getAuthorId()).isEqualTo(user);
                    assertThat(a.getQuestionId()).isEqualTo(question);
                    assertThat(a.getCreatedAt()).isBeforeOrEqualTo(Instant.now());
                    assertThat(a.getVotes()).isEqualTo(Collections.emptyList());
                    assertThat(a.getText()).isEqualTo(answerPostDto.getText());
                });
    }
}