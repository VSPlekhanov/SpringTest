package com.epam.lstrsum.aggregators;

import com.epam.lstrsum.controller.UserRuntimeRequestComponent;
import com.epam.lstrsum.converter.AnswerDtoMapper;
import com.epam.lstrsum.converter.QuestionDtoMapper;
import com.epam.lstrsum.converter.UserDtoMapper;
import com.epam.lstrsum.persistence.AnswerRepository;
import com.epam.lstrsum.persistence.QuestionRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static com.epam.lstrsum.testutils.InstantiateUtil.someAnswer;
import static com.epam.lstrsum.testutils.InstantiateUtil.someAnswerPostDto;
import static com.epam.lstrsum.testutils.InstantiateUtil.someQuestion;
import static com.epam.lstrsum.testutils.InstantiateUtil.someString;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

public class AnswerAggregatorTest {
    private AnswerAggregator aggregator;

    @Mock
    private UserAggregator userAggregator;

    @Mock
    private AnswerRepository answerRepository;

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private UserDtoMapper userMapper;

    @Mock
    private AnswerDtoMapper answerMapper;

    @Mock
    private QuestionDtoMapper questionMapper;

    @Mock
    private UserRuntimeRequestComponent userRuntimeRequestComponent;

    @Before
    public void setUp() {
        initMocks(this);
        aggregator = new AnswerAggregator(
                answerMapper, userMapper,
                questionMapper, answerRepository,
                questionRepository, userAggregator,
                userRuntimeRequestComponent
        );

    }

    @Test
    public void modelToAllFieldsDto() throws Exception {
        aggregator.modelToAllFieldsDto(someAnswer());

        verify(userMapper, times(1)).modelToBaseDto(any());
        verify(questionMapper, times(1)).modelToBaseDto(any(), any());
        verify(answerMapper, times(1))
                .modelToAllFieldsDto(any(), any(), any());
    }

    @Test
    public void modelToBaseDto() throws Exception {
        aggregator.modelToBaseDto(someAnswer());

        verify(userMapper, times(1)).modelToBaseDto(any());
        verify(answerMapper, times(1)).modelToBaseDto(any(), any(), any());
    }

    @Test
    public void answersToQuestionInAnswerBaseDto() throws Exception {
        aggregator.answersToQuestionInAnswerBaseDto(someQuestion());

        verify(userMapper, times(1)).usersToListOfBaseDtos(any());
        verify(answerRepository, times(1)).findAnswersByQuestionIdOrderByCreatedAtAsc(any());
        verify(answerMapper, times(1)).answersToQuestionInAnswerBaseDto(any(), any(), any());
    }

    @Test
    public void answerPostDtoAndAuthorEmailToAnswer() throws Exception {
        aggregator.answerPostDtoAndAuthorEmailToAnswer(someQuestion(), someAnswerPostDto(), someString());

        verify(userAggregator, times(1)).findByEmail(anyString());
        verify(answerMapper, times(1))
                .answerPostDtoAndAuthorEmailToAnswer(any(), any(), any());
    }
}
