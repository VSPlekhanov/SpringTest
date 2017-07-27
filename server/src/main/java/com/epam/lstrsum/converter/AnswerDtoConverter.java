package com.epam.lstrsum.converter;

import com.epam.lstrsum.dto.answer.AnswerAllFieldsDto;
import com.epam.lstrsum.dto.answer.AnswerBaseDto;
import com.epam.lstrsum.dto.answer.AnswerPostDto;
import com.epam.lstrsum.model.Answer;
import com.epam.lstrsum.model.Request;
import com.epam.lstrsum.service.AnswerService;
import com.epam.lstrsum.service.RequestService;
import com.epam.lstrsum.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class AnswerDtoConverter implements BasicModelDtoConverter<Answer, AnswerBaseDto>,
        AllFieldModelDtoConverter<Answer, AnswerAllFieldsDto> {

    @Autowired
    private UserDtoConverter userConverter;
    @Autowired
    private RequestDtoConverter requestConverter;
    @Autowired
    private AnswerService answerService;
    @Autowired
    private UserService userService;
    @Autowired
    private RequestService requestService;

    @Override
    public AnswerAllFieldsDto modelToAllFieldsDto(Answer answer) {
        return new AnswerAllFieldsDto(answer.getText(), answer.getCreatedAt(),
                userConverter.modelToBaseDto(answer.getAuthorId()), answer.getUpVote(),
                answer.getAnswerId(), requestConverter.modelToBaseDto(answer.getParentId()));
    }

    @Override
    public AnswerBaseDto modelToBaseDto(Answer answer) {
        return new AnswerBaseDto(answer.getText(), answer.getCreatedAt(),
                userConverter.modelToBaseDto(answer.getAuthorId()), answer.getUpVote());
    }

    public List<AnswerBaseDto> answersToRequestInAnswerBaseDto(Request request) {
        List<Answer> answersToRequest = answerService.findAnswersToThis(request);
        List<AnswerBaseDto> answerBaseDtos = new ArrayList<>();
        answersToRequest.forEach(a -> answerBaseDtos.add(modelToBaseDto(a)));

        return answerBaseDtos;
    }

    public Answer answerPostDtoAndAuthorEmailToAnswer(AnswerPostDto answerPostDto, String email) {
        Answer newAnswer = new Answer();
        newAnswer.setUpVote(0);
        newAnswer.setText(answerPostDto.getText());
        newAnswer.setCreatedAt(Instant.now());

        newAnswer.setParentId(requestService.getRequestById(answerPostDto.getParentId()));

        // Instant can parse only this format of date "2017-11-29T10:15:30Z"
        // throws DateTimeException if RequestPostDto got wrong data format
        newAnswer.setAuthorId(userService.getUserByEmail(email));
        newAnswer.setUpVote(0);
        return newAnswer;
    }
}
