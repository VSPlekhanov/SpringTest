package com.epam.lstrsum.service;

import com.epam.lstrsum.SetUpDataBaseCollections;
import com.epam.lstrsum.controller.AnswerController;
import com.epam.lstrsum.dto.answer.AnswerBaseDto;
import com.epam.lstrsum.model.Answer;
import com.epam.lstrsum.persistence.AnswerRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class VoteServiceTest extends SetUpDataBaseCollections {

    @Autowired
    private AnswerController answerController;

    @Autowired
    private VoteService voteService;

    @Autowired
    private AnswerRepository answerRepository;

    @Test
    public void voteForAnswerTest() {
        when(userRuntimeRequestComponent.getEmail()).thenReturn("Tyler_Derden@mylo.com");

        String answerIdWithoutVotes = "1u_2r_1a";
        String questionId = "1u_2r";
        String someUserEmail = "Tyler_Derden@mylo.com";
        String someOtherUserEmail = "Steven_Tyler@epam.com";

        List<AnswerBaseDto> answers = answerController.getAnswersByQuestionId(questionId, -1, -1).getBody();
        assertThat(answers).hasSize(2);

        AnswerBaseDto answerBaseDto = answers.get(0);
        assertThat(answerBaseDto.getUserVoted()).isFalse();
        assertThat(answerBaseDto.getUpVote()).isEqualTo(0);

        answerBaseDto = answers.get(1);
        assertThat(answerBaseDto.getUserVoted()).isFalse();

        assertThat(answerRepository.findOne(answerIdWithoutVotes).getVotes().size()).isEqualTo(0);

        assertThat(voteService.voteForAnswerByUser(answerIdWithoutVotes, someUserEmail)).isTrue();

        Answer answer = answerRepository.findOne(answerIdWithoutVotes);

        assertThat(answer.getVotes()).hasSize(1);
        assertThat(answer.getVotes().get(0).getAuthorEmail()).isEqualTo("Tyler_Derden@mylo.com");

        List<AnswerBaseDto> answersAfter = answerController.getAnswersByQuestionId(questionId, -1, -1).getBody();
        assertThat(answersAfter).hasSize(2);

        AnswerBaseDto answerBaseDtoAfter = answersAfter.get(0);
        assertThat(answerBaseDtoAfter.getUserVoted()).isTrue();
        assertThat(answerBaseDtoAfter.getUpVote()).isEqualTo(1);

        answerBaseDtoAfter = answersAfter.get(1);
        assertThat(answerBaseDtoAfter.getUserVoted()).isFalse();

        assertThat(voteService.voteForAnswerByUser(answerIdWithoutVotes, someOtherUserEmail)).isTrue();

        List<AnswerBaseDto> answersAfterOther = answerController.getAnswersByQuestionId(questionId, -1, -1).getBody();
        assertThat(answersAfterOther).hasSize(2);

        AnswerBaseDto answerBaseDtoAfterOther = answersAfterOther.get(0);
        assertThat(answerBaseDtoAfterOther.getUserVoted()).isTrue();
        assertThat(answerBaseDtoAfterOther.getUpVote()).isEqualTo(2);

        answerBaseDtoAfter = answersAfter.get(1);
        assertThat(answerBaseDtoAfter.getUserVoted()).isFalse();
    }

    @Test
    public void voteForAnswerTwice() {
        String answerIdAlreadyVoted = "1u_1r_3a";
        String userWhoVoteAnswer = "John_Doe@epam.com";

        assertThat(answerRepository.findOne(answerIdAlreadyVoted).getVotes().size()).isEqualTo(4);

        assertThat(voteService.voteForAnswerByUser(answerIdAlreadyVoted, userWhoVoteAnswer))
                .isTrue();

        assertThat(answerRepository.findOne(answerIdAlreadyVoted).getVotes())
                .hasSize(4);
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
