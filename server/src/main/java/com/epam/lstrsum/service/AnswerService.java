package com.epam.lstrsum.service;

import com.epam.lstrsum.converter.AnswerDtoConverter;
import com.epam.lstrsum.dto.answer.AnswerAllFieldsDto;
import com.epam.lstrsum.dto.answer.AnswerPostDto;
import com.epam.lstrsum.exception.AnswerValidationException;
import com.epam.lstrsum.model.Answer;
import com.epam.lstrsum.model.Request;
import com.epam.lstrsum.persistence.AnswerRepository;
import com.epam.lstrsum.persistence.RequestRepository;
import com.epam.lstrsum.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.isNull;

@Service
@RequiredArgsConstructor
public class AnswerService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private AnswerDtoConverter answerDtoConverter;

    private final AnswerRepository answerRepository;

    public AnswerAllFieldsDto addNewAnswer(AnswerPostDto answerPostDto, String email) {
        validateAnswerData(answerPostDto, email);
        Answer newAnswer = answerDtoConverter.answerPostDtoAndAuthorEmailToAnswer(answerPostDto, email);
        Answer saved = answerRepository.save(newAnswer);
        return answerDtoConverter.modelToAllFieldsDto(saved);
    }

    public AnswerAllFieldsDto answerToDto(Answer answer) {
        return answerDtoConverter.modelToAllFieldsDto(answer);
    }

    private void validateAnswerData(AnswerPostDto answerPostDto, String email) {
        if (isNull(answerPostDto)) {
            throw new AnswerValidationException("Answer must be not null!");
        }
        if (isNull(email) || email.trim().isEmpty()) {
            throw new AnswerValidationException("Author must be not null or empty!");
        } else if (!userRepository.findByEmail(email).isPresent()) {
            throw new AnswerValidationException("No such user!");
        }
        if (isNull(answerPostDto.getText()) || answerPostDto.getText().trim().isEmpty()) {
            throw new AnswerValidationException("Null or empty fields found in answer " + answerPostDto.toJson());
        }
        if (isNull(answerPostDto.getParentId()) || answerPostDto.getParentId().trim().isEmpty()) {
            throw new AnswerValidationException("Parent is null or empty" + answerPostDto.getParentId());
        } else if (isNull(requestRepository.findOne(answerPostDto.getParentId()))) {
            throw new AnswerValidationException("No such request!");
        }


    }

    public List<Answer> findAnswersToThis(Request request) {
        List<Answer> answersToRequest = answerRepository.findAnswersByParentIdOrderByCreatedAtAsc(request);
        return answersToRequest != null ? answersToRequest : new ArrayList<>();
    }
}
