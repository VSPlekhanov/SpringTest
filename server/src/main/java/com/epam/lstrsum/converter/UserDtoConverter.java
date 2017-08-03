package com.epam.lstrsum.converter;

import com.epam.lstrsum.dto.user.UserAllFieldsDto;
import com.epam.lstrsum.dto.user.UserBaseDto;
import com.epam.lstrsum.model.User;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserDtoConverter implements BasicModelDtoConverter<User, UserBaseDto>,
        AllFieldModelDtoConverter<User, UserAllFieldsDto> {

    @Override
    public UserAllFieldsDto modelToAllFieldsDto(User user) {
        return new UserAllFieldsDto(user.getUserId(), user.getFirstName(), user.getLastName(), user.getEmail(),
                user.getRoles(), user.getCreatedAt(), user.getIsActive());
    }

    @Override
    public UserBaseDto modelToBaseDto(User user) {
        return new UserBaseDto(user.getUserId(), user.getFirstName(), user.getLastName(), user.getEmail());
    }

    public List<UserBaseDto> allowedSubsToListOfUserBaseDtos(List<User> allowedSubs) {
        return allowedSubs.stream()
                .map(this::modelToBaseDto)
                .collect(Collectors.toList());
    }
}
