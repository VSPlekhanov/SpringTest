package com.epam.lstrsum.converter;


import com.epam.lstrsum.dto.request.RequestAllFieldsDto;
import com.epam.lstrsum.dto.request.RequestAppearanceDto;
import com.epam.lstrsum.dto.request.RequestBaseDto;
import com.epam.lstrsum.dto.request.RequestPostDto;
import com.epam.lstrsum.model.Request;
import com.epam.lstrsum.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RequestDtoConverter implements BasicModelDtoConverter<Request, RequestBaseDto>,
        AllFieldModelDtoConverter<Request, RequestAllFieldsDto> {

    @Autowired
    private UserService userService;
    @Autowired
    private UserDtoConverter userConverter;
    @Autowired
    private AnswerDtoConverter answerConverter;

    @Override
    public RequestAllFieldsDto modelToAllFieldsDto(Request request) {
        return new RequestAllFieldsDto(request.getRequestId(), request.getTitle(),
                request.getTags(), request.getCreatedAt(), request.getDeadLine(),
                userConverter.modelToBaseDto(request.getAuthorId()), request.getUpVote(),
                userConverter.allowedSubsToListOfUserBaseDtos(request.getAllowedSubs()), request.getText());
    }

    @Override
    public RequestBaseDto modelToBaseDto(Request request) {
        return new RequestBaseDto(request.getRequestId(), request.getTitle(),
                request.getTags(), request.getCreatedAt(), request.getDeadLine(),
                userConverter.modelToBaseDto(request.getAuthorId()), request.getUpVote());
    }

    public RequestAppearanceDto modelToRequestAppearanceDto(Request request) {
        return new RequestAppearanceDto(request.getRequestId(), request.getTitle(),
                request.getTags(), request.getCreatedAt(), request.getDeadLine(),
                userConverter.modelToBaseDto(request.getAuthorId()), request.getUpVote(),
                request.getText(), answerConverter.answersToRequestInAnswerBaseDto(request));
    }

    public Request requestPostDtoAndAuthorEmailToRequest(RequestPostDto requestPostDto, String email) {
        return Request.builder()
                .title(requestPostDto.getTitle())
                .tags(requestPostDto.getTags())
                .text(requestPostDto.getText())
                .createdAt(Instant.now())
                // Instant can parse only this format of date "2017-11-29T10:15:30Z"
                // throws DateTimeException if RequestPostDto got wrong data format
                .deadLine(Instant.ofEpochMilli(requestPostDto.getDeadLine()))
                .authorId(userService.getUserByEmail(email))
                .allowedSubs(requestPostDto.getAllowedSubs().stream()
                        .map(userEmail -> userService.getUserByEmail(userEmail))
                        .collect(Collectors.toList()))
                .upVote(0)
                .build();
    }

    public List<RequestBaseDto> subscriptionsToListOfRequestBaseDto(List<Request> subscriptions) {
        return subscriptions.stream()
                .map(this::modelToBaseDto)
                .collect(Collectors.toList());
    }
}

