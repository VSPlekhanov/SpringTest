package com.epam.lstrsum.converter;

import com.epam.lstrsum.SetUpDataBaseCollections;
import com.epam.lstrsum.dto.user.UserBaseDto;
import com.epam.lstrsum.model.User;
import com.epam.lstrsum.persistence.UserRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;

public class UserDtoConverterTest extends SetUpDataBaseCollections {
    @Autowired
    private UserDtoConverter userConverter;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void ConvertModelToAllFieldsDtoReturnsExpectedValue() throws Exception {
        User user = userRepository.findOne("1u");

        assertThat(userConverter.modelToAllFieldsDto(user))
                .hasFieldOrPropertyWithValue("isActive", user.getIsActive())
                .hasFieldOrPropertyWithValue("createdAt", user.getCreatedAt())
                .hasFieldOrPropertyWithValue("roles", user.getRoles())
                .hasFieldOrPropertyWithValue("userId", user.getUserId())
                .hasFieldOrPropertyWithValue("email", user.getEmail())
                .hasFieldOrPropertyWithValue("firstName", user.getFirstName())
                .hasFieldOrPropertyWithValue("lastName", user.getLastName());
    }

    @Test
    public void ConvertModelToBaseDtoReturnsExpectedValue() throws Exception {
        User user = userRepository.findOne("1u");

        assertThat(userConverter.modelToBaseDto(user))
                .hasFieldOrPropertyWithValue("userId", user.getUserId())
                .hasFieldOrPropertyWithValue("firstName", user.getFirstName())
                .hasFieldOrPropertyWithValue("lastName", user.getLastName())
                .hasFieldOrPropertyWithValue("email", user.getEmail());

    }

    @Test
    public void allowedSubsToListOfUserBaseDtos() throws Exception {
        List<User> users = Collections.singletonList(userRepository.findOne("1u"));
        List<UserBaseDto> userBaseDtos = userConverter.allowedSubsToListOfUserBaseDtos(users);

        assertThat(userBaseDtos.size(), is(equalTo(users.size())));
    }
}
