package com.epam.lstrsum.service;

import com.epam.lstrsum.converter.RequestDtoConverter;
import com.epam.lstrsum.dto.request.RequestAllFieldsDto;
import com.epam.lstrsum.dto.request.RequestAppearanceDto;
import com.epam.lstrsum.dto.request.RequestBaseDto;
import com.epam.lstrsum.dto.request.RequestPostDto;
import com.epam.lstrsum.exception.RequestValidationException;
import com.epam.lstrsum.mail.EmailNotification;
import com.epam.lstrsum.mail.template.NewRequestNotificationTemplate;
import com.epam.lstrsum.model.Request;
import com.epam.lstrsum.persistence.RequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;


@Service
@RequiredArgsConstructor
public class RequestService {

    private final static int REQUEST_TITLE_LENGTH = 5;
    private final static int REQUEST_TEXT_LENGTH = 5;

    private final RequestDtoConverter requestDtoConverter;
    private final RequestRepository requestRepository;

    public List<RequestAllFieldsDto> findAll() {
        List<Request> requestList = requestRepository.findAll();
        return mapList(requestList, requestDtoConverter::modelToAllFieldsDto);
    }

    public List<RequestAllFieldsDto> search(String searchQuery) {
        List<Request> requestList = requestRepository.search(searchQuery);
        return mapList(requestList, requestDtoConverter::modelToAllFieldsDto);
    }

    public List<RequestBaseDto> findAllRequestsBaseDto(int requestPage, int requestAmount) {
        Pageable pageable = new PageRequest(requestPage, requestAmount);
        List<Request> requestList = requestRepository.findAllByOrderByCreatedAtDesc(pageable);
        return mapList(requestList, requestDtoConverter::modelToBaseDto);
    }

    @EmailNotification(template = NewRequestNotificationTemplate.class)
    public RequestAllFieldsDto addNewRequest(RequestPostDto requestPostDto, String email) {
        validateRequestData(requestPostDto, email);
        Request newRequest = requestDtoConverter.requestPostDtoAndAuthorEmailToRequest(requestPostDto, email);
        Request saved = requestRepository.save(newRequest);
        return requestDtoConverter.modelToAllFieldsDto(saved);
    }

    public RequestAllFieldsDto getRequestAllFieldDtoByRequestId(String requestId) {
        Request request = requestRepository.findOne(requestId);
        return requestDtoConverter.modelToAllFieldsDto(request);
    }

    public RequestAppearanceDto getRequestAppearanceDtoByRequestId(String requestId) {
        Request request = requestRepository.findOne(requestId);
        return requestDtoConverter.modelToRequestAppearanceDto(request);
    }

    public Request getRequestById(String requestId){
        return requestRepository.findOne(requestId);
    }

    public boolean contains(String objectsId) {
        return requestRepository.findOne(objectsId) != null;
    }

    private void validateRequestData(RequestPostDto requestPostDto, String email) {
        if (requestPostDto == null) {
            throw new RequestValidationException("Post request should have json for RequestPostDto");
        }
        if (email == null) {
            throw new RequestValidationException("probably should`nt appear at all, problems with SSO");
        }
        if ((requestPostDto.getText() == null) || (requestPostDto.getTitle() == null)) {
            throw new RequestValidationException("null fields found in request " + requestPostDto.toJson());
        }
        if (requestPostDto.getTitle().length() < REQUEST_TITLE_LENGTH) {
            throw new RequestValidationException("Title is too short " + requestPostDto.toJson());
        }
        if (requestPostDto.getText().length() < REQUEST_TEXT_LENGTH) {
            throw new RequestValidationException("Text is too short " + requestPostDto.toJson());
        }
    }

    private static<T1,T2> List<T2> mapList(List<T1> list, Function<T1, T2> mapper) {
        List<T2> result = new ArrayList<>();

        for (T1 value : list) {
            result.add(mapper.apply(value));
        }

        return result;
    }
}
