package com.epam.lstrsum.aggregators;

import com.epam.lstrsum.converter.QuestionDtoMapper;
import com.epam.lstrsum.converter.UserDtoMapper;
import com.epam.lstrsum.dto.question.QuestionPostDto;
import com.epam.lstrsum.testutils.InstantiateUtil;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.List;

import static com.epam.lstrsum.testutils.InstantiateUtil.someQuestion;
import static com.epam.lstrsum.testutils.InstantiateUtil.someQuestionPostDto;
import static com.epam.lstrsum.testutils.InstantiateUtil.someString;
import static com.epam.lstrsum.utils.FunctionalUtil.getListWithSize;
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
        verify(userMapper, times(1)).usersToListOfUserBaseDtos(any());
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
    public void questionPostDtoAndAuthorEmailAndAttachmentsToQuestion() throws Exception {
        final QuestionPostDto questionPostDto = someQuestionPostDto();
        List<String> attachmentIds = getListWithSize(InstantiateUtil::someString, 2);
        aggregator.questionPostDtoAndAuthorEmailAndAttachmentsToQuestion(questionPostDto, someString(), attachmentIds);

        verify(userAggregator, times(questionPostDto.getAllowedSubs().size() + 1))
                .findByEmail(anyString());
        verify(questionMapper, times(1))
                .questionPostDtoAndAuthorEmailAndAttachmentsToQuestion(any(), any(), any(), any());
    }

    @Test
    public void subscriptionsToListOfQuestionBaseDto() throws Exception {
        final int size = 2;
        aggregator.subscriptionsToListOfQuestionBaseDto(getListWithSize(InstantiateUtil::someQuestion, size));

        verify(userMapper, times(size)).modelToBaseDto(any());
        verify(questionMapper, times(1)).subscriptionsToListOfQuestionBaseDto(any(), any());
    }
}
