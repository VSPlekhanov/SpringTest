package com.epam.lstrsum.aggregators;

import com.epam.lstrsum.converter.QuestionDtoMapper;
import com.epam.lstrsum.converter.UserDtoMapper;
import com.epam.lstrsum.dto.question.QuestionPostDto;
import com.epam.lstrsum.model.Question;
import com.epam.lstrsum.model.User;
import com.epam.lstrsum.persistence.AttachmentRepository;
import com.epam.lstrsum.testutils.InstantiateUtil;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.List;

import static com.epam.lstrsum.testutils.InstantiateUtil.*;
import static com.epam.lstrsum.utils.FunctionalUtil.getList;
import static com.epam.lstrsum.utils.FunctionalUtil.getListWithSize;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

public class QuestionAggregatorTest {
    private QuestionAggregator questionAggregator;

    @Mock
    private UserAggregator userAggregator;

    @Mock
    private AttachmentRepository attachmentRepository;

    @Mock
    private UserDtoMapper userMapper;

    @Mock
    private QuestionDtoMapper questionMapper;

    @Before
    public void setUp() {
        initMocks(this);
        questionAggregator = new QuestionAggregator(
                userMapper, questionMapper,
                attachmentRepository, userAggregator
        );
    }

    @Test
    public void modelToAllFieldsDto() throws Exception {
        questionAggregator.modelToAllFieldsDto(someQuestion());

        verify(userMapper, times(1)).modelToBaseDto(any());
        verify(userMapper, times(1)).usersToListOfBaseDtos(any());
        verify(questionMapper, times(1)).modelToAllFieldsDto(any(), any(), any());
    }

    @Test
    public void modelToBaseDto() throws Exception {
        questionAggregator.modelToBaseDto(someQuestion());

        verify(userMapper, times(1)).modelToBaseDto(any());
        verify(questionMapper, times(1)).modelToBaseDto(any(), any());
    }

    @Test
    public void modelToQuestionAppearanceDto() throws Exception {
        questionAggregator.modelToQuestionAppearanceDto(someQuestion(), someString());

        verify(userMapper, times(1)).modelToBaseDto(any());
        verify(attachmentRepository, times(1)).findAll(anyList());
        verify(questionMapper, times(1)).modelToQuestionAppearanceDto(any(), any(), any(), anyBoolean());
    }

    @Test
    public void questionPostDtoAndAuthorEmailToQuestion() throws Exception {
        final QuestionPostDto questionPostDto = someQuestionPostDto();
        questionAggregator.questionPostDtoAndAuthorEmailToQuestion(questionPostDto, someString());

        verify(userAggregator, times(questionPostDto.getAllowedSubs().size() + 1))
                .findByEmail(anyString());
        verify(questionMapper, times(1))
                .questionPostDtoAndAuthorEmailToQuestion(any(), any(), any(), any());
    }

    @Test
    public void questionPostDtoAndAuthorEmailAndAttachmentsToQuestion() throws Exception {
        final QuestionPostDto questionPostDto = someQuestionPostDto();
        List<String> attachmentIds = getListWithSize(InstantiateUtil::someString, 2);
        questionAggregator.questionPostDtoAndAuthorEmailAndAttachmentsToQuestion(questionPostDto, someString(), attachmentIds);

        verify(userAggregator, times(questionPostDto.getAllowedSubs().size() + 1))
                .findByEmail(anyString());
        verify(questionMapper, times(1))
                .questionPostDtoAndAuthorEmailAndAttachmentsToQuestion(any(), any(), any(), any(), any());
    }

    @Test
    public void subscriptionsToListOfQuestionBaseDto() throws Exception {
        final int size = 2;
        questionAggregator.subscriptionsToListOfQuestionBaseDto(getListWithSize(InstantiateUtil::someQuestion, size));

        verify(userMapper, times(size)).modelToBaseDto(any());
        verify(questionMapper, times(1)).subscriptionsToListOfQuestionBaseDto(any(), any());
    }

    @Test
    public void currentUserSubscribeQuestionTest(){
        Question question = someQuestion();
        List<User> subscribers = getList(InstantiateUtil::someUser);
        User currentUser = someUser();
        subscribers.add(currentUser);
        question.setSubscribers(subscribers);

        questionAggregator.modelToQuestionAppearanceDto(question, currentUser.getEmail());
        verify(questionMapper).modelToQuestionAppearanceDto(any(), any(), any(), eq(true));
    }

    @Test
    public void currentUserNotSubscribeQuestionTest(){
        Question question = someQuestion();
        List<User> subscribers = getList(InstantiateUtil::someUser);
        User currentUser = someUser();
        subscribers.remove(currentUser);
        question.setSubscribers(subscribers);

        questionAggregator.modelToQuestionAppearanceDto(question, currentUser.getEmail());
        verify(questionMapper).modelToQuestionAppearanceDto(any(), any(), any(), eq(false));
    }


}
