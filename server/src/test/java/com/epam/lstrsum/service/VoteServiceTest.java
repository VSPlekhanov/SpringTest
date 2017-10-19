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

    @Test
    public void voteForAnswerTest() {
        String answerIdWithoutVotes = "1u_2r_1a";
        String someUserEmail = "Tyler_Derden@mylo.com";

        int beforeVote = answerRepository.findOne(answerIdWithoutVotes).getVotes().size();

        assertThat(voteService.voteForAnswerByUser(answerIdWithoutVotes, someUserEmail))
                .isTrue();

        assertThat(answerRepository.findOne(answerIdWithoutVotes).getVotes())
                .hasSize(beforeVote + 1);
    }

    @Test
    public void voteForAnswerTwice() {
        String answerIdAlreadyVoted = "1u_1r_3a";
        String userWhoVoteAnswer = "John_Doe@epam.com";

        int beforeVote = answerRepository.findOne(answerIdAlreadyVoted).getVotes().size();

        assertThat(voteService.voteForAnswerByUser(answerIdAlreadyVoted, userWhoVoteAnswer))
                .isTrue();

        assertThat(answerRepository.findOne(answerIdAlreadyVoted).getVotes())
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

        Answer beforeUnvoting = answerRepository.findOne(answerIdAlreadyVoted);

        assertThat(voteService.unvoteForAnswerByUser(answerIdAlreadyVoted, userWhoVoteAnswer))
                .isTrue();

        Answer afterUnvoting = answerRepository.findOne(answerIdAlreadyVoted);

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
