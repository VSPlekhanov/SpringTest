package com.epam.lstrsum.service;

import com.epam.lstrsum.converter.VoteDtoConverter;
import com.epam.lstrsum.dto.vote.VoteAllFieldsDto;
import com.epam.lstrsum.exception.BusinessLogicException;
import com.epam.lstrsum.model.Answer;
import com.epam.lstrsum.model.User;
import com.epam.lstrsum.model.Vote;
import com.epam.lstrsum.persistence.VoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class VoteService {
    private static final int INCREASE_AMOUNT = 1;
    private static final int DECREASE_AMOUNT = -1;

    @Autowired
    private VoteRepository voteRepository;
    @Autowired
    private AnswerService answerService;
    @Autowired
    private UserService userService;
    @Autowired
    private VoteDtoConverter voteDtoConverter;

    public Optional<Vote> getVoteByUserAndAnswerId(User user, String answerId) {
        return voteRepository.findVoteByUserIdAndAnswerId(user, answerId);
    }

    public VoteAllFieldsDto addVoteToAnswer(String email, String answerId) {
        User user = userService.getUserByEmail(email);
        Answer answer = answerService.getAnswerById(answerId);
        Optional<Vote> vote = getVoteByUserAndAnswerId(user, answerId);

        checkUserAbilityToAddVote(vote, email);
        updateAnswerVoteCounter(answer, INCREASE_AMOUNT);

        Vote saved;
        if (vote.isPresent()) {
            vote.get().setIsRevoked(false);
            saved = voteRepository.save(vote.get());
        } else {
            saved = voteRepository.save(createNewVoteForAnswerByUser(user, answer));
        }
        return voteDtoConverter.modelToAllFieldsDto(saved);
    }

    private void checkUserAbilityToAddVote(Optional<Vote> vote, String email) {
        if (vote.isPresent() && !vote.get().getIsRevoked()) {
            log.error("User with email = {} already voted for answer id = {}", email, vote.get().getAnswerId().getAnswerId());
            throw new BusinessLogicException("User with email = " + email + " already voted for answer id = " + vote.get().getAnswerId().getAnswerId());
        }
    }

    private Vote createNewVoteForAnswerByUser(User user, Answer answer) {
        return Vote.builder()
                .createdAt(Instant.now())
                .isRevoked(false)
                .userId(user)
                .answerId(answer)
                .build();
    }

    private void updateAnswerVoteCounter(Answer answer, int amount) {
        if ((answer.getUpVote() + amount) < 0) {
            log.error("Answer id = {} cannot be updated to negative value. Current vote counter value = {}, updated on value= {}",
                    answer.getAnswerId(), answer.getUpVote(), amount);
            throw new BusinessLogicException("Answer cannot be updated to negative value");
        }
        answer.setUpVote(answer.getUpVote() + amount);
        answerService.save(answer);
    }

    public List<VoteAllFieldsDto> findAllVotesForAnswer(String answerId) {
        List<Vote> votesForAnswerList = voteRepository.findAllByAnswerId(answerId);
        return votesForAnswerList
                .stream()
                .map(voteDtoConverter::modelToAllFieldsDto)
                .collect(Collectors.toList());
    }

    public void deleteVoteToAnswer(String email, String answerId) {

        User user = userService.getUserByEmail(email);
        Answer answer = answerService.getAnswerById(answerId);

        Optional<Vote> vote = getVoteByUserAndAnswerId(user, answerId);

        checkUserAbilityToDeleteVote(vote, email);
        updateAnswerVoteCounter(answer, DECREASE_AMOUNT);

        vote.get().setIsRevoked(true);
        voteRepository.save(vote.get());
    }

    private void checkUserAbilityToDeleteVote(Optional<Vote> vote, String email) {
        if (!vote.isPresent() || vote.get().getIsRevoked()) {
            log.error("User with email = {} didn't vote or already revoked his vote", email);
            throw new BusinessLogicException("User with email = " + email + " didn't vote or already revoked his vote");
        }
    }
}
