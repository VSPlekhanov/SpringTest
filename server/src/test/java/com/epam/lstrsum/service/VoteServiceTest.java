package com.epam.lstrsum.service;

import com.epam.lstrsum.SetUpDataBaseCollections;
import com.epam.lstrsum.model.Answer;
import com.epam.lstrsum.persistence.AnswerRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

public class VoteServiceTest extends SetUpDataBaseCollections {
    @Autowired
    private VoteService voteService;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private AnswerService answerService;

    @Test
    public void voteForAnswerTest() {
        String answerIdWithoutVotes = "1u_2r_1a";
        String someUserEmail = "Tyler_Derden@mylo.com";
        String questionId = questionService.getQuestionByAnswerId(answerIdWithoutVotes).getQuestionId();

        int beforeVote =  answerService.getAnswerByIdAndQuestionId(answerIdWithoutVotes, questionId).getVotes().size();

        assertThat(voteService.voteForAnswerByUser(answerIdWithoutVotes, someUserEmail))
                .isTrue();

        assertThat(answerService.getAnswerByIdAndQuestionId(answerIdWithoutVotes, questionId).getVotes())
                .hasSize(beforeVote + 1);
    }

    @Test
    public void voteForAnswerTwice() {
        String answerIdAlreadyVoted = "1u_1r_3a";
        String userWhoVoteAnswer = "John_Doe@epam.com";
        String questionId = questionService.getQuestionByAnswerId(answerIdAlreadyVoted).getQuestionId();

        int beforeVote = answerService.getAnswerByIdAndQuestionId(answerIdAlreadyVoted, questionId).getVotes().size();

        assertThat(voteService.voteForAnswerByUser(answerIdAlreadyVoted, userWhoVoteAnswer))
                .isTrue();

        assertThat(answerService.getAnswerByIdAndQuestionId(answerIdAlreadyVoted, questionId).getVotes())
                .hasSize(beforeVote);
    }

    @Test
    public void voteAnswerNotExists() {
        String someNotExistingAnswerId = "someNotExistingAnswerId";

        assertThat(voteService.voteForAnswerByUser(someNotExistingAnswerId, "someUser"))
                .isFalse();
    }

    @Test
    public void unVoteAnswer() {
        String answerIdAlreadyVoted = "1u_1r_3a";
        String userWhoVoteAnswer = "John_Doe@epam.com";
        String questionId = questionService.getQuestionByAnswerId(answerIdAlreadyVoted).getQuestionId();

        Answer beforeUnvoting = answerService.getAnswerByIdAndQuestionId(answerIdAlreadyVoted, questionId);

        assertThat(voteService.unvoteForAnswerByUser(answerIdAlreadyVoted, userWhoVoteAnswer))
                .isTrue();

        Answer afterUnvoting = answerService.getAnswerByIdAndQuestionId(answerIdAlreadyVoted, questionId);

        assertThat(afterUnvoting.getVotes())
                .hasSize(beforeUnvoting.getVotes().size() - 1);
    }

    @Test
    public void unVoteAnswerNotExists() {
        String someNotExistingAnswerId = "someNotExistingAnswerId";

        assertThat(voteService.unvoteForAnswerByUser(someNotExistingAnswerId, "someUser"))
                .isFalse();
    }

}
