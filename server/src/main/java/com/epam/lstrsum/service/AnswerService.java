package com.epam.lstrsum.service;

import com.epam.lstrsum.model.Answer;
import com.epam.lstrsum.model.Request;
import com.epam.lstrsum.persistence.AnswerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AnswerService {

    private AnswerRepository answerRepository;

    @Autowired
    public AnswerService(AnswerRepository answerRepository) {
        this.answerRepository = answerRepository;
    }

    public List<Answer> findAll() {
        return answerRepository.findAll();
    }

    public List<Answer> findAnswersToThis(Request request) {
        List<Answer> answersToRequest = answerRepository.findAnswersByParentIdOrderByCreatedAtAsc(request);
        return answersToRequest != null ? answersToRequest : new ArrayList<>();
    }
}
