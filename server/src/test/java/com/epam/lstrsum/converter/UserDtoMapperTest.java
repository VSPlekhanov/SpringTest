package com.epam.lstrsum.converter;

import com.epam.lstrsum.SetUpDataBaseCollections;
import com.epam.lstrsum.dto.user.UserBaseDto;
import com.epam.lstrsum.dto.user.telescope.TelescopeDataDto;
import com.epam.lstrsum.enums.UserRoleType;
import com.epam.lstrsum.model.User;
import com.epam.lstrsum.testutils.InstantiateUtil;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import static com.epam.lstrsum.testutils.InstantiateUtil.initList;
import static com.epam.lstrsum.testutils.InstantiateUtil.someRoles;
import static com.epam.lstrsum.testutils.InstantiateUtil.someString;
import static com.epam.lstrsum.testutils.InstantiateUtil.someTelescopeDataDto;
import static com.epam.lstrsum.testutils.InstantiateUtil.someUser;
import static org.assertj.core.api.Assertions.assertThat;

public class UserDtoMapperTest extends SetUpDataBaseCollections {
    @Autowired
    private UserDtoMapper userMapper;

    public static void checkUserBaseDto(UserBaseDto userBaseDto, User user) {
        assertThat(userBaseDto.getEmail()).isEqualTo(user.getEmail());
        assertThat(userBaseDto.getFirstName()).isEqualTo(user.getFirstName());
        assertThat(userBaseDto.getLastName()).isEqualTo(user.getLastName());
        assertThat(userBaseDto.getUserId()).isEqualTo(user.getUserId());
    }

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

        assertThat(userMapper.usersToListOfUserBaseDtos(users))
                .hasSize(users.size());
    }

    @Test
    public void userTelescopeInfoDtoToUser() throws Exception {
        final TelescopeDataDto userDto = someTelescopeDataDto();
        final String email = someString();
        final List<UserRoleType> roles = someRoles();

        assertThat(userMapper.userTelescopeInfoDtoToUser(userDto, email, roles))
                .satisfies(e -> {
                    assertThat(e.getEmail()).isEqualTo(email);
                    assertThat(e.getFirstName()).isEqualTo(userDto.getFirstName());
                    assertThat(e.getLastName()).isEqualTo(userDto.getLastName());
                    assertThat(e.getRoles()).isEqualTo(roles);
                    assertThat(e.getCreatedAt()).isBeforeOrEqualTo(Instant.now());
                    assertThat(e.getIsActive()).isFalse();
                });
    }
}