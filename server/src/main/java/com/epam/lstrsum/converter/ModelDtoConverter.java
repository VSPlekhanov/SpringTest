package com.epam.lstrsum.converter;

import com.epam.lstrsum.dto.request.RequestAllFieldsDto;
import com.epam.lstrsum.dto.request.RequestBaseDto;
import com.epam.lstrsum.dto.request.RequestPostDto;
import com.epam.lstrsum.dto.user.UserAllFieldsDto;
import com.epam.lstrsum.dto.user.UserBaseDto;
import com.epam.lstrsum.model.Request;
import com.epam.lstrsum.model.User;
import com.epam.lstrsum.dto.*;
import com.epam.lstrsum.model.*;
import com.epam.lstrsum.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class ModelDtoConverter {


    @Autowired
    private UserService userService;

    public RequestAllFieldsDto requestToAllFieldsDto(Request request) {
        List<User> allowedSubs = request.getAllowedSubs();
        List<UserBaseDto> userBaseDtos = new ArrayList<>();
        for (User user : allowedSubs) {
            userBaseDtos.add(userToBaseDto(user));
        }
        return new RequestAllFieldsDto(request.getRequestId(), request.getTitle(),
                request.getTags(), request.getCreatedAt(), request.getDeadLine(),
                userToBaseDto(request.getAuthorId()), request.getUpVote(), userBaseDtos, request.getText());
    }

    public UserAllFieldsDto userToAllFieldDto(User user) {
        return new UserAllFieldsDto(user.getUserId(), user.getFirstName(), user.getLastName(), user.getEmail(),
                user.getRoles(), user.getCreatedAt(), user.getIsActive());
    }

    public AttachmentAllFieldsDto attachmentToAllFieldDto(Attachment attachment) {
        return new AttachmentAllFieldsDto(attachment.getId(), attachment.getName(), attachment.getType(), attachment.getData());
    }

    public Request requestDtoAndAuthorEmailToRequest(RequestPostDto requestPostDto, String email) {
        Request newRequest = new Request();
        newRequest.setTitle(requestPostDto.getTitle());
        newRequest.setTags(requestPostDto.getTags());
        newRequest.setText(requestPostDto.getText());
        newRequest.setCreatedAt(Instant.now());
        // Instant can parse only this format of date "2017-11-29T10:15:30Z"
        // throws DateTimeException if RequestPostDto got wrong data format
        newRequest.setDeadLine(Instant.parse(requestPostDto.getDeadLine()));
        newRequest.setAuthorId(userService.getUserByEmail(email));
        List<String> subsFromDto = requestPostDto.getAllowedSubs();
        List<User> subsForRequest = new ArrayList<>();
        for (String userEmail : subsFromDto) {
            subsForRequest.add(userService.getUserByEmail(userEmail));
        }
        newRequest.setAllowedSubs(subsForRequest);
        newRequest.setUpVote(0);
        return newRequest;
    }


    public RequestBaseDto requestToBaseDto(Request request) {
        /*
          - requestId
          - title
          - author's name
          - creation date
          - deadline date
          - tags
          - upVote
        */
        RequestBaseDto requestBaseDto = new RequestBaseDto(request.getRequestId(), request.getTitle(),
                request.getTags(), request.getCreatedAt(), request.getDeadLine(), userToBaseDto(request.getAuthorId()),
                request.getUpVote());
        return requestBaseDto;
    }

    public UserBaseDto userToBaseDto(User user) {
        return new UserBaseDto(user.getUserId(), user.getFirstName(), user.getLastName(), user.getEmail());
    }


}
