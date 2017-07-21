package com.epam.lstrsum.dto.subscription;

import com.epam.lstrsum.dto.request.RequestBaseDto;
import com.epam.lstrsum.dto.user.UserBaseDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class SubscriptionAllFieldsDto {
    private String subscriptionId;
    private UserBaseDto userId;
    private List<RequestBaseDto> requestIds;
}
