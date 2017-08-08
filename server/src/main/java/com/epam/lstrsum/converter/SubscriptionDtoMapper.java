package com.epam.lstrsum.converter;

import com.epam.lstrsum.dto.question.QuestionBaseDto;
import com.epam.lstrsum.dto.subscription.SubscriptionAllFieldsDto;
import com.epam.lstrsum.dto.user.UserBaseDto;
import com.epam.lstrsum.model.Subscription;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SubscriptionDtoMapper {

    @Mappings({
            @Mapping(target = "questionIds", source = "questionIds"),
            @Mapping(target = "userId", source = "userId")
    })
    SubscriptionAllFieldsDto modelToAllFieldsDto(Subscription subscription, UserBaseDto userId, List<QuestionBaseDto> questionIds);
}
