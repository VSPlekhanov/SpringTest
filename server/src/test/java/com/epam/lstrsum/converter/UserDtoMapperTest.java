package com.epam.lstrsum.converter;

import com.epam.lstrsum.InstantiateUtil;
import com.epam.lstrsum.SetUpDataBaseCollections;
import com.epam.lstrsum.dto.user.UserBaseDto;
import com.epam.lstrsum.enums.UserRoleType;
import com.epam.lstrsum.model.User;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

import static com.epam.lstrsum.InstantiateUtil.initList;
import static com.epam.lstrsum.InstantiateUtil.someUser;
import static org.assertj.core.api.Assertions.assertThat;

public class UserDtoMapperTest extends SetUpDataBaseCollections {
    @Autowired
    private UserDtoMapper userMapper;

    @Test
    public void modelToAllFieldsDto() throws Exception {
        final User user = someUser();
        List<String> roles = user.getRoles().stream().map(UserRoleType::name).collect(Collectors.toList());

        assertThat(userMapper.modelToAllFieldsDto(user))
                .satisfies(u -> {
                    checkUserBaseDto(u, user);
                    assertThat(u.getCreatedAt()).isEqualTo(user.getCreatedAt());
                    assertThat(u.getIsActive()).isEqualTo(user.getIsActive());
                    assertThat(u.getRoles()).isEqualTo(roles.toArray());
                });
    }

    @Test
    public void modelToBaseDto() throws Exception {
        final User user = someUser();

        assertThat(userMapper.modelToBaseDto(user))
                .satisfies(u -> checkUserBaseDto(u, user));
    }

    @Test
    public void allowedSubsToListOfUserBaseDtos() throws Exception {
        final List<User> users = initList(InstantiateUtil::someUser);

        assertThat(userMapper.allowedSubsToListOfUserBaseDtos(users))
                .hasSize(users.size());
    }

    public static void checkUserBaseDto(UserBaseDto userBaseDto, User user) {
        assertThat(userBaseDto.getEmail()).isEqualTo(user.getEmail());
        assertThat(userBaseDto.getFirstName()).isEqualTo(user.getFirstName());
        assertThat(userBaseDto.getLastName()).isEqualTo(user.getLastName());
        assertThat(userBaseDto.getUserId()).isEqualTo(user.getUserId());
    }
}