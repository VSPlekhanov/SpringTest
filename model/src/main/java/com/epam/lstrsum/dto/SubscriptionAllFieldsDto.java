package com.epam.lstrsum.dto;

import com.epam.lstrsum.model.Request;
import com.epam.lstrsum.model.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class SubscriptionAllFieldsDto {
    private String subscriptionId;
    private User userId;
    private List<Request> requestIds;
}
