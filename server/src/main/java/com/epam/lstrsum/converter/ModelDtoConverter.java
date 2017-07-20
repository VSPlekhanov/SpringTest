package com.epam.lstrsum.converter;

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
        return new RequestAllFieldsDto(request.getRequestId(), request.getTitle(),
                request.getTags(), request.getText(), request.getCreatedAt(), request.getDeadLine(),
                request.getAuthorId(), request.getAllowedSubs(), request.getUpVote());
    }

    public AnswerAllFieldsDto answerToAllFieldsDto(Answer answer) {
        return new AnswerAllFieldsDto(answer.getAnswerId(), answer.getParentId(), answer.getText(),
                answer.getCreatedAt(), answer.getAuthorId(), answer.getUpVote());
    }

    public SubscriptionAllFieldsDto subscriptionToAllFieldDto(Subscription subscription) {
        return new SubscriptionAllFieldsDto(subscription.getSubscriptionId(), subscription.getUserId(),
                subscription.getRequestIds());
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
        // Instant can parse only this format of date "2017-11-29T10:15:30Z"
        newRequest.setCreatedAt(Instant.now());
        newRequest.setDeadLine(Instant.parse(requestPostDto.getDeadLine()));
        newRequest.setAuthorId(userService.getUserByEmail(email));
        List<String> subsFromDto = requestPostDto.getAllowedSubs();
        List<User> subsForRequest = new ArrayList<>();
        //TODO Dunno what we will get in this collection, Users ids or emails, we should decide about it, then reimplement this
        for (String userEmail : subsFromDto) {
            subsForRequest.add(userService.getUserByEmail(userEmail));
        }
        newRequest.setAllowedSubs(subsForRequest);
        newRequest.setUpVote(0);
        return newRequest;
    }


}
