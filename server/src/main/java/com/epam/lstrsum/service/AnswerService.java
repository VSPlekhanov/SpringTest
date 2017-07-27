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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AnswerService {

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RequestRepository requestRepository;


    @Autowired
    private AnswerDtoConverter answerDtoConverter;

    @Autowired
    public AnswerService(AnswerRepository answerRepository) {
        this.answerRepository = answerRepository;
    }

    public List<AnswerAllFieldsDto> findAll() {
        List<Answer> requestList = answerRepository.findAll();
        List<AnswerAllFieldsDto> dtoList = new ArrayList<>();
        for (Answer answer : requestList) {
            dtoList.add(answerDtoConverter.modelToAllFieldsDto(answer));
        }
        return dtoList;
    }

    public Answer addNewAnswer(AnswerPostDto answerPostDto, String email) {
        validateAnswerData(answerPostDto, email);
        Answer newAnswer = answerDtoConverter.answerPostDtoAndAuthorEmailToAnswer(answerPostDto, email);
        answerRepository.save(newAnswer);
        return newAnswer;
    }


    public AnswerAllFieldsDto answerToDto(Answer answer) {
        return answerDtoConverter.modelToAllFieldsDto(answer);
    }


    private void validateAnswerData(AnswerPostDto answerPostDto, String email) {
        if (answerPostDto == null) {
            throw new AnswerValidationException("Answer must be not null!");
        }
        if(email==null){
            throw new AnswerValidationException("Author must be not null!");
        }
        else if(!userRepository.findByEmail(email).isPresent()){
            throw new AnswerValidationException("No such user!");
        }
        if (answerPostDto.getText() == null) {
            throw new AnswerValidationException("null fields found in answer " + answerPostDto.toJson());
        }
        if (answerPostDto.getParentId() == null) {
            throw new AnswerValidationException("Parent is null " + answerPostDto.getParentId());
        }
        else if(requestRepository.findOne(answerPostDto.getParentId())==null){
            throw new AnswerValidationException("No such request!");
        }
    }

    public List<Answer> findAnswersToThis(Request request) {
        List<Answer> answersToRequest = answerRepository.findAnswersByParentIdOrderByCreatedAtAsc(request);
        return answersToRequest != null ? answersToRequest : new ArrayList<>();
    }
}
