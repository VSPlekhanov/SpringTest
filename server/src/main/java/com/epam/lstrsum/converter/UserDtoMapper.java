package com.epam.lstrsum.converter;

import com.epam.lstrsum.dto.user.UserAllFieldsDto;
import com.epam.lstrsum.dto.user.UserBaseDto;
import com.epam.lstrsum.dto.user.telescope.TelescopeDataDto;
import com.epam.lstrsum.enums.UserRoleType;
import com.epam.lstrsum.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserDtoMapper {
    @Mappings({
            @Mapping(target = "createdAt", source = "createdAt")
    })
    UserAllFieldsDto modelToAllFieldsDto(User user);

    @Mappings({
            @Mapping(target = "email", source = "email")
    })
    UserBaseDto modelToBaseDto(User user);

    default List<UserBaseDto> allowedSubsToListOfUserBaseDtos(List<User> allowedSubs) {
        return allowedSubs.stream()
                .map(this::modelToBaseDto)
                .collect(Collectors.toList());
    }

    @Mappings({
            @Mapping(target = "createdAt", expression = "java(java.time.Instant.now())"),
            @Mapping(target = "isActive", constant = "true"),
            @Mapping(target = "email", source = "email"),
            @Mapping(target = "roles", source = "roles"),
            @Mapping(target = "userId", ignore = true)
    })
    User userTelescopeInfoDtoToUser(TelescopeDataDto userDto, String email, List<UserRoleType> roles);
}
