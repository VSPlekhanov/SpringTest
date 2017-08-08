package com.epam.lstrsum.service;

import com.epam.lstrsum.SetUpDataBaseCollections;
import com.epam.lstrsum.dto.user.UserBaseDto;
import com.epam.lstrsum.dto.vote.VoteAllFieldsDto;
import com.epam.lstrsum.exception.BusinessLogicException;
import com.epam.lstrsum.exception.NoSuchAnswerException;
import com.epam.lstrsum.exception.NoSuchUserException;
import com.epam.lstrsum.model.Answer;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class VoteServiceTest extends SetUpDataBaseCollections {
    private static final String alreadyVotedUserEmail = "John_Doe@epam.com";
    private static final String notVotedUserEmail = "Bob_Hoplins@epam.com";
    private static final String oneMoreNotVotedUserEmail = "Ernest_Hemingway@epam.com";
    private static final List<String> notVotedUserEmailsList = Arrays.asList("Bob_Hoplins@epam.com", "Ernest_Hemingway@epam.com", "Steven_Tyler@epam.com");
    private static final String answerId = "1u_2r_1a";
    private static final String nonExistingAnswerId = "17635r929";
    private static final String answerIdWithNegativeVoteAmount = "4u_5r_3a";

    @Autowired
    private AnswerService answerService;
    @Autowired
    private VoteService voteService;
    @Autowired
    private UserService userService;


    @Test(expected = BusinessLogicException.class)
    public void addVoteToAnswerWithAlreadyVotedUser() {
        voteService.addVoteToAnswer(alreadyVotedUserEmail, answerId);
    }

    @Test(expected = NoSuchUserException.class)
    public void addVoteToAnswerWithNullEmail() {
        voteService.addVoteToAnswer(null, answerId);
    }

    @Test(expected = NoSuchAnswerException.class)
    public void addVoteToAnswerWithEmptyAnswerId() {
        voteService.addVoteToAnswer(alreadyVotedUserEmail, "       ");
    }

    @Test
    public void addVoteToAnswer() {
        VoteAllFieldsDto voteDto = voteService.addVoteToAnswer(notVotedUserEmail, answerId);
        assertNotNull(voteDto);
        assertThat(voteDto.isRevoked(), is(false));
        assertTrue(voteDto.getCreatedAt().isBefore(Instant.now()));

        UserBaseDto userDto = voteDto.getUserBaseDto();
        assertThat(userDto.getUserId(), is("2u"));
        assertThat(userDto.getEmail(), is(notVotedUserEmail));
        assertThat(userDto.getFirstName(), is("Bob"));
        assertThat(userDto.getLastName(), is("Hoplins"));
    }

    @Test(expected = BusinessLogicException.class)
    public void addVoteToAnswerWithNegativeVotesAmount() {
        voteService.addVoteToAnswer(notVotedUserEmail, answerIdWithNegativeVoteAmount);
    }

    @Test(expected = NoSuchAnswerException.class)
    public void deleteVoteWithNonExistingAnswerId() {
        voteService.deleteVoteToAnswer(alreadyVotedUserEmail, nonExistingAnswerId);
    }

    @Test(expected = BusinessLogicException.class)
    public void deleteVoteWithNonVotedUser() {
        voteService.deleteVoteToAnswer(oneMoreNotVotedUserEmail, answerId);
    }

    @Test
    public void deleteVote() {
        voteService.deleteVoteToAnswer(alreadyVotedUserEmail, answerId);
        VoteAllFieldsDto revokedVote = voteService.findAllVotesForAnswer(answerId)
                .stream()
                .filter(v -> userService.getUserById(v.getUserBaseDto().getUserId()).getEmail().equals(alreadyVotedUserEmail))
                .collect(Collectors.toList()).get(0);

        assertThat(revokedVote.isRevoked(), is(true));
    }

    @Test
    public void findAllVotesForAnswer() {
        List<VoteAllFieldsDto> listWithVotes = voteService.findAllVotesForAnswer(answerId);

        List<String> votesIds = listWithVotes.stream().map(VoteAllFieldsDto::getVoteId).collect(Collectors.toList());

        assertThat(votesIds, hasItems("1u_2r_1a_1v", "1u_2r_1a_2v", "1u_2r_1a_3v"));
    }

    @Test
    public void addVotesThenDeleteVotes() {
        Answer answerBeforeMultipleVoteAdding = answerService.getAnswerById(answerId);
        assertThat(answerBeforeMultipleVoteAdding.getUpVote(), is(3));
        assertThat(answerBeforeMultipleVoteAdding.getAnswerId(), is("1u_2r_1a"));
        assertThat(answerBeforeMultipleVoteAdding.getQuestionId().getQuestionId(), is("1u_2r"));

        notVotedUserEmailsList.forEach(e -> voteService.addVoteToAnswer(e, answerId));

        Answer answerAfterMultipleVoteAdding = answerService.getAnswerById(answerId);
        assertThat(answerAfterMultipleVoteAdding.getUpVote(), is(6));

        notVotedUserEmailsList.subList(0, 2).forEach(e -> voteService.deleteVoteToAnswer(e, answerId));

        Answer answerAfterMultipleVoteDeleting = answerService.getAnswerById(answerId);
        assertThat(answerAfterMultipleVoteDeleting.getUpVote(), is(4));
        assertEquals(answerAfterMultipleVoteDeleting.getAnswerId(), answerBeforeMultipleVoteAdding.getAnswerId());
        assertEquals(answerAfterMultipleVoteDeleting.getQuestionId().getQuestionId(), answerBeforeMultipleVoteAdding.getQuestionId().getQuestionId());
    }
}
