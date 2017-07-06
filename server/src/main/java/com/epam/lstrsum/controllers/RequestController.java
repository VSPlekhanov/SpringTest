package com.epam.lstrsum.controllers;

import com.epam.lstrsum.model.Request;
import com.epam.lstrsum.service.RequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/api/request")
public class RequestController {

    private final RequestService requestService;

    @Autowired
    public RequestController(RequestService requestService) {
        this.requestService = requestService;
    }

    @RequestMapping
    public List<Request> getListOfRequests() {
        return requestService.findAll();
    }
}
