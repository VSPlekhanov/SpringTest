package com.epam.lstrsum.converter;


import com.epam.lstrsum.SetUpDataBaseCollections;
import com.epam.lstrsum.dto.question.QuestionAllFieldsDto;
import com.epam.lstrsum.dto.question.QuestionAppearanceDto;
import com.epam.lstrsum.dto.question.QuestionBaseDto;
import com.epam.lstrsum.dto.question.QuestionPostDto;
import com.epam.lstrsum.dto.user.UserBaseDto;
import com.epam.lstrsum.model.Question;
import com.epam.lstrsum.model.User;
import com.epam.lstrsum.persistence.QuestionRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;

public class QuestionDtoConverterTest extends SetUpDataBaseCollections {

    @Autowired
    private QuestionDtoConverter questionDtoConverter;

    @Autowired
    private UserDtoConverter userConverter;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private AnswerDtoConverter answerConverter;

    @Test
    public void convertFromPostDtoToQuestionReturnsExpectedValue() {
        QuestionPostDto postDto = new QuestionPostDto("this the end", new String[]{"1", "2", "3", "go"},
                "just some text", 1501145960439L,
                Arrays.asList("Bob_Hoplins@epam.com", "Tyler_Greeds@epam.com",
                        "Donald_Gardner@epam.com", "Ernest_Hemingway@epam.com"));
        String authorEmail = "John_Doe@epam.com";
        Question convertedQuestion = questionDtoConverter.questionPostDtoAndAuthorEmailToQuestion(postDto, authorEmail);
        assertThat(postDto.getTitle(), is(equalTo(convertedQuestion.getTitle())));
        assertThat(postDto.getTags(), is(equalTo(convertedQuestion.getTags())));
        assertThat(Instant.ofEpochMilli(postDto.getDeadLine()), is(equalTo(convertedQuestion.getDeadLine())));
        assertThat(postDto.getText(), is(equalTo(convertedQuestion.getText())));
        List<String> subsFromQuestion = convertedQuestion.getAllowedSubs().stream().map(User::getEmail).collect(Collectors.toList());
        assertThat(postDto.getAllowedSubs(), is(equalTo(subsFromQuestion)));

    }

    @Test
    public void convertQuestionToDtoReturnsExpectedValue() {
        Question question = questionRepository.findOne("1u_1r");
        QuestionAllFieldsDto allFieldsDto = questionDtoConverter.modelToAllFieldsDto(question);

        List<UserBaseDto> userBaseDtos = userConverter.allowedSubsToListOfUserBaseDtos(question.getAllowedSubs());

        assertThat(question.getQuestionId(), is(equalTo(allFieldsDto.getQuestionId())));
        assertThat(question.getTitle(), is(equalTo(allFieldsDto.getTitle())));
        assertThat(question.getTags(), is(equalTo(allFieldsDto.getTags())));
        assertThat(question.getText(), is(equalTo(allFieldsDto.getText())));
        assertThat(question.getCreatedAt(), is(equalTo(allFieldsDto.getCreatedAt())));
        assertThat(question.getDeadLine(), is(equalTo(allFieldsDto.getDeadLine())));
        assertThat(userConverter.modelToBaseDto(question.getAuthorId()), is(equalTo(allFieldsDto.getAuthor())));
        assertThat(userBaseDtos, is(equalTo(allFieldsDto.getAllowedSubs())));
        assertThat(question.getUpVote(), is(equalTo(allFieldsDto.getUpVote())));
    }


    @Test
    public void converterIsAbleToCreateQuestionWithEmptySubList() {
        QuestionPostDto postDto = new QuestionPostDto("this the end", new String[]{"1", "2", "3", "go"},
                "just some text", 1501100960439L,
                Collections.emptyList());
        String authorEmail = "John_Doe@epam.com";
        Question question = questionDtoConverter.questionPostDtoAndAuthorEmailToQuestion(postDto, authorEmail);
        assertThat(question, notNullValue());
    }

    @Test
    public void converterIsAbleToCreateQuestionWithEmptyTagsArray() {
        QuestionPostDto postDto = new QuestionPostDto("this the end", new String[0],
                "just some text", 1501111960439L,
                Collections.emptyList());
        String authorEmail = "John_Doe@epam.com";
        Question question = questionDtoConverter.questionPostDtoAndAuthorEmailToQuestion(postDto, authorEmail);
        assertThat(question, notNullValue());
    }

    @Test
    public void convertFromModelToQuestionAppearanceDtoReturnsExcpectedValue() {
        Question question = questionRepository.findOne("1u_1r");
        QuestionAppearanceDto questionAppearanceDto = questionDtoConverter.modelToQuestionAppearanceDto(question);

        assertThat(questionAppearanceDto)
                .satisfies(
                        q -> {
                            assertThat(q.getQuestionId()).isEqualTo(question.getQuestionId());
                            assertThat(q.getTitle()).isEqualTo(question.getTitle());
                            assertThat(q.getTags()).isEqualTo(question.getTags());
                            assertThat(q.getText()).isEqualTo(question.getText());
                            assertThat(q.getCreatedAt()).isEqualTo(question.getCreatedAt());
                            assertThat(q.getUpVote()).isEqualTo(question.getUpVote());
                            assertThat(q.getDeadLine()).isEqualTo(question.getDeadLine());
                            assertThat(q.getAuthor()).isEqualTo(userConverter.modelToBaseDto(question.getAuthorId()));
                            assertThat(q.getAnswers()).isEqualTo(answerConverter.answersToQuestionInAnswerBaseDto(question));
                        }
                );

    }

    @Test
    public void subscriptionsToListOfQuestionBaseDto() {
        List<Question> questions = Collections.singletonList(questionRepository.findOne("1u_1r"));
        List<QuestionBaseDto> questionBaseDtos = questionDtoConverter.subscriptionsToListOfQuestionBaseDto(questions);

        assertThat(questionBaseDtos.size(), is(equalTo(questions.size())));
    }
}
