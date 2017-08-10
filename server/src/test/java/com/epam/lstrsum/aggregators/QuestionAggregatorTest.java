package com.epam.lstrsum.aggregators;

import com.epam.lstrsum.InstantiateUtil;
import com.epam.lstrsum.converter.QuestionDtoMapper;
import com.epam.lstrsum.converter.UserDtoMapper;
import com.epam.lstrsum.dto.question.QuestionPostDto;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static com.epam.lstrsum.InstantiateUtil.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

public class QuestionAggregatorTest {
    private QuestionAggregator aggregator;

    @Mock
    private UserAggregator userAggregator;

    @Mock
    private AnswerAggregator answerAggregator;

    @Mock
    private UserDtoMapper userMapper;

    @Mock
    private QuestionDtoMapper questionMapper;

    @Before
    public void setUp() {
        initMocks(this);
        aggregator = new QuestionAggregator(
                userMapper, questionMapper,
                answerAggregator, userAggregator
        );
    }

    @Test
    public void modelToAllFieldsDto() throws Exception {
        aggregator.modelToAllFieldsDto(someQuestion());

        verify(userMapper, times(1)).modelToBaseDto(any());
        verify(userMapper, times(1)).allowedSubsToListOfUserBaseDtos(any());
        verify(questionMapper, times(1)).modelToAllFieldsDto(any(), any(), any());
    }

    @Test
    public void modelToBaseDto() throws Exception {
        aggregator.modelToBaseDto(someQuestion());

        verify(userMapper, times(1)).modelToBaseDto(any());
        verify(questionMapper, times(1)).modelToBaseDto(any(), any());
    }

    @Test
    public void modelToQuestionAppearanceDto() throws Exception {
        aggregator.modelToQuestionAppearanceDto(someQuestion());

        verify(userMapper, times(1)).modelToBaseDto(any());
        verify(answerAggregator, times(1)).answersToQuestionInAnswerBaseDto(any());
        verify(questionMapper, times(1)).modelToQuestionAppearanceDto(any(), any(), any());
    }

    @Test
    public void questionPostDtoAndAuthorEmailToQuestion() throws Exception {
        final QuestionPostDto questionPostDto = someQuestionPostDto();
        aggregator.questionPostDtoAndAuthorEmailToQuestion(questionPostDto, someString());

        verify(userAggregator, times(questionPostDto.getAllowedSubs().size() + 1))
                .findByEmail(anyString());
        verify(questionMapper, times(1))
                .questionPostDtoAndAuthorEmailToQuestion(any(), any(), any());
    }

    @Test
    public void subscriptionsToListOfQuestionBaseDto() throws Exception {
        final int size = 2;
        aggregator.subscriptionsToListOfQuestionBaseDto(initList(InstantiateUtil::someQuestion, size));

        verify(userMapper, times(size)).modelToBaseDto(any());
        verify(questionMapper, times(1)).subscriptionsToListOfQuestionBaseDto(any(), any());
    }
}