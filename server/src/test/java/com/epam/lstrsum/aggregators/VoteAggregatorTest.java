package com.epam.lstrsum.aggregators;

import com.epam.lstrsum.converter.AnswerDtoMapper;
import com.epam.lstrsum.converter.UserDtoMapper;
import com.epam.lstrsum.converter.VoteDtoMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static com.epam.lstrsum.InstantiateUtil.someVote;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

public class VoteAggregatorTest {
    private VoteAggregator voteAggregator;

    @Mock
    private UserDtoMapper userMapper;

    @Mock
    private AnswerDtoMapper answerMapper;

    @Mock
    private VoteDtoMapper voteMapper;

    @Before
    public void setUp() {
        initMocks(this);
        voteAggregator = new VoteAggregator(userMapper, answerMapper, voteMapper);
    }

    @Test
    public void modelToAllFieldsDto() throws Exception {
        voteAggregator.modelToAllFieldsDto(someVote());

        verify(userMapper, times(1)).modelToBaseDto(any());
        verify(answerMapper, times(1)).modelToBaseDto(any(), any());
        verify(voteMapper, times(1))
                .modelToAllFieldsDto(any(), any(), any());
    }

}