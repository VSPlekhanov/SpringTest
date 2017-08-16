package com.epam.lstrsum.service;

import com.epam.lstrsum.dto.answer.AnswerAllFieldsDto;
import com.epam.lstrsum.dto.answer.AnswerPostDto;
import com.epam.lstrsum.email.EmailNotification;
import com.epam.lstrsum.email.template.NewAnswerNotificationTemplate;
import com.epam.lstrsum.model.Answer;

public interface AnswerService {
    @EmailNotification(template = NewAnswerNotificationTemplate.class)
    AnswerAllFieldsDto addNewAnswer(AnswerPostDto answerPostDto, String email);

    Answer getAnswerById(String answerId);

    void save(Answer answer);
}
