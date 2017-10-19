package com.epam.lstrsum.service;

import com.epam.lstrsum.dto.answer.AnswerAllFieldsDto;
import com.epam.lstrsum.dto.answer.AnswerBaseDto;
import com.epam.lstrsum.dto.answer.AnswerPostDto;
import com.epam.lstrsum.email.EmailNotification;
import com.epam.lstrsum.email.template.NewAnswerNotificationTemplate;
import com.epam.lstrsum.model.Answer;
import com.epam.lstrsum.model.Question;
import com.epam.lstrsum.model.QuestionWithAnswersCount;

import java.util.List;

public interface AnswerService {
    @EmailNotification(template = NewAnswerNotificationTemplate.class)
    AnswerAllFieldsDto addNewAnswer(AnswerPostDto answerPostDto, String email);

    Answer getAnswerById(String answerId);

    void save(Answer answer, String questionId);

    List<QuestionWithAnswersCount> aggregateToCount(List<Question> questions);

    List<AnswerBaseDto> getAnswersByQuestionId(String questionId, int page, int size);

    List<AnswerBaseDto> getAnswersByQuestionId(String questionId);

    Long getAnswerCountByQuestionId(String questionId);
}
