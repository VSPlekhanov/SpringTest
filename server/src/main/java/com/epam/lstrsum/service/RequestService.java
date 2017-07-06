package com.epam.lstrsum.service;

import com.epam.lstrsum.model.Request;
import com.epam.lstrsum.persistence.RequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class RequestService {

    private final RequestRepository requestRepository;

    @Autowired
    public RequestService(RequestRepository requestRepository) {
        this.requestRepository = requestRepository;
    }

    public List<Request> findAll(){
        return requestRepository.findAll();
    }
}
