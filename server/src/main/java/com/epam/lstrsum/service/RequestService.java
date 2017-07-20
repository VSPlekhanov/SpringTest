package com.epam.lstrsum.service;

import com.epam.lstrsum.converter.ModelDtoConverter;
import com.epam.lstrsum.dto.RequestAllFieldsDto;
import com.epam.lstrsum.dto.RequestPostDto;
import com.epam.lstrsum.exception.RequestValidationException;
import com.epam.lstrsum.model.Request;
import com.epam.lstrsum.persistence.RequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
public class RequestService {

    private final static int REQUEST_TITLE_LENGTH = 5;
    private final static int REQUEST_TEXT_LENGTH = 5;

    @Autowired
    private ModelDtoConverter modelDtoConverter;

    @Autowired
    private RequestRepository requestRepository;

    public List<RequestAllFieldsDto> findAll() {
        List<Request> requestList = requestRepository.findAll();
        List<RequestAllFieldsDto> dtoList = new ArrayList<>();
        for (Request request : requestList) {
            dtoList.add(modelDtoConverter.requestToAllFieldsDto(request));
        }
        return dtoList;
    }

    public Request addNewRequest(RequestPostDto requestPostDto, String email) {
        validateRequestData(requestPostDto, email);
        Request newRequest = modelDtoConverter.requestDtoAndAuthorEmailToRequest(requestPostDto, email);
        requestRepository.save(newRequest);
        return newRequest;
    }

    public RequestAllFieldsDto getRequestDtoByRequestId(String requestId) {
        Request request = requestRepository.findOne(requestId);
        return modelDtoConverter.requestToAllFieldsDto(request);
    }

    public RequestAllFieldsDto requestToDto(Request request) {
        return modelDtoConverter.requestToAllFieldsDto(request);
    }

    public boolean contains(String objectsId) {
        return requestRepository.findOne(objectsId) != null;
    }

    private void validateRequestData(RequestPostDto requestPostDto, String email) {
        if ((requestPostDto.getText() == null) || (requestPostDto.getTitle() == null)) {
            throw new RequestValidationException("null fields found in request " + requestPostDto.toJson());
        }
        if (requestPostDto.getTitle().length() < REQUEST_TITLE_LENGTH) {
            throw new RequestValidationException("Title is too short " + requestPostDto.toJson());
        }
        if (requestPostDto.getTitle().length() < REQUEST_TEXT_LENGTH) {
            throw new RequestValidationException("Text is too short " + requestPostDto.toJson());
        }
    }
}
