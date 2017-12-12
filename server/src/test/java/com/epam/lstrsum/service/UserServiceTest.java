package com.epam.lstrsum.service;

import com.epam.lstrsum.SetUpDataBaseCollections;
import com.epam.lstrsum.aggregators.UserAggregator;
import com.epam.lstrsum.dto.user.telescope.TelescopeDataDto;
import com.epam.lstrsum.dto.user.telescope.TelescopeEmployeeEntityDto;
import com.epam.lstrsum.exception.NoSuchUserException;
import com.epam.lstrsum.model.User;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.val;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.epam.lstrsum.enums.UserRoleType.*;
import static com.epam.lstrsum.testutils.InstantiateUtil.*;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anySetOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;


public class UserServiceTest extends SetUpDataBaseCollections {

    @MockBean
    private TelescopeService telescopeService;

    @Autowired
    private UserService userService;

    @MockBean
    private UserAggregator userAggregator;

    @Test
    public void getUserByEmail() {
        User user = userService.findUserByEmailOrThrowException(SOME_USER_EMAIL);

        assertNotNull(user);
        assertEquals(user.getEmail(), SOME_USER_EMAIL);
    }

    public void findAllWithRole() {
        assertEquals(userService.findAllWithRole(ROLE_EXTENDED_USER).size(), 5);
    }

    @Test
    public void findAll() {
        final List<User> allUsers = userService.findAll();

        assertThat(allUsers)
                .hasSize(7)
                .anySatisfy(user -> assertThat(user.getEmail()).isEqualTo(SOME_USER_EMAIL));
    }

    @Test
    public void updateActiveMoreOne() {
        final String johnDoe = "John_Doe@epam.com";
        final String bobHoplins = "Bob_Hoplins@epam.com";
        final long actual = userService.setActiveForAllAs(Arrays.asList(johnDoe, bobHoplins), false);

        assertEquals(actual, 2);
        assertEquals(userService.findUserByEmailOrThrowException(johnDoe).getIsActive(), false);
        assertEquals(userService.findUserByEmailOrThrowException(bobHoplins).getIsActive(), false);
    }

    @Test
    public void updateActiveNoOne() {
        final long actual = userService.setActiveForAllAs(Arrays.asList("first", "second", "third"), false);

        assertEquals(actual, 0);
    }

    @Test
    public void getUserById() {
        final User user = userService.findUserById(EXISTING_USER_ID);

        assertNotNull(user);
        assertEquals(user.getUserId(), EXISTING_USER_ID);
    }

    @Test
    public void addIfNotExistAllWithRole() {
        final List<String> alreadyInBaseEmails =
                userService.findAll().stream().filter(user -> user.getRoles().contains(ROLE_SIMPLE_USER)).map(User::getEmail)
                        .collect(Collectors.toList());
        List<TelescopeEmployeeEntityDto> dtos = Collections.singletonList(someTelescopeEmployeeEntityDto());
        final List<String> notInBaseEmails = dtos.stream()
                .map(TelescopeEmployeeEntityDto::getData)
                .map(data -> data.getEmail().get(0))
                .collect(Collectors.toList());

        final List<String> concat = Stream.concat(alreadyInBaseEmails.stream(), notInBaseEmails.stream()).collect(Collectors.toList());

        doReturn(dtos).when(telescopeService).getUsersInfoByEmails(anySetOf(String.class));
        when(userAggregator.userTelescopeInfoDtoToUser(any(), anyString(), any())).thenReturn(someUser());

        final long actual = userService.addIfNotExistAllWithRole(concat, ROLE_EXTENDED_USER);

        assertEquals(actual, notInBaseEmails.size());

        verify(telescopeService, times(1)).getUsersInfoByEmails(anySetOf(String.class));
        verify(userAggregator, times(1)).userTelescopeInfoDtoToUser(any(), anyString(), any());
    }

    @Test
    public void addIfNotExistAllWithRoleWrongArgs() {
        assertEquals(userService.addIfNotExistAllWithRole(null, ROLE_SIMPLE_USER), 0);
        assertEquals(userService.addIfNotExistAllWithRole(emptyList(), ROLE_SIMPLE_USER), 0);
        assertEquals(userService.addIfNotExistAllWithRole(someStrings(), null), 0);

        doReturn(emptyList()).when(telescopeService).getUsersInfoByEmails(anySetOf(String.class));
        assertEquals(userService.addIfNotExistAllWithRole(someStrings(), ROLE_SIMPLE_USER), 0);

        verify(telescopeService, times(1)).getUsersInfoByEmails(anySetOf(String.class));
    }

    @Test
    public void addIfNotExistAllWithRoleNoDataFromTelescope() {
        String someEmail = someString();
        TelescopeDataDto dataDto = TelescopeDataDto.builder().email(singletonList(someEmail)).build();
        TelescopeEmployeeEntityDto employeeEntityDto = TelescopeEmployeeEntityDto.builder().data(dataDto).build();
        List<TelescopeEmployeeEntityDto> dtoList = Collections.singletonList(employeeEntityDto);

        doReturn(dtoList).when(telescopeService).getUsersInfoByEmails(anySetOf(String.class));

        userService.addIfNotExistAllWithRole(Collections.singletonList(someEmail), ROLE_SIMPLE_USER);

        verify(telescopeService, times(1)).getUsersInfoByEmails(anySetOf(String.class));
    }

    @Test
    public void addIfNotExistAllWithRoleAlreadyAddedUser() {
        String someEmail = SOME_USER_EMAIL.toLowerCase();
        TelescopeDataDto dataDto =
                TelescopeDataDto.builder().email(singletonList(someEmail)).lastName(someString()).firstName(someString()).build();
        TelescopeEmployeeEntityDto employeeEntityDto = TelescopeEmployeeEntityDto.builder().data(dataDto).build();
        List<TelescopeEmployeeEntityDto> dtoList = Collections.singletonList(employeeEntityDto);

        doReturn(dtoList).when(telescopeService).getUsersInfoByEmails(anySetOf(String.class));

        userService.addIfNotExistAllWithRole(Collections.singletonList(someEmail), ROLE_SIMPLE_USER);

        verify(telescopeService, times(1)).getUsersInfoByEmails(anySetOf(String.class));
    }

    @Test
    public void addIfNotExistAllWithRoleAndSomeWrongEmails() {
        val existentEmail = "telescope_user_to_be_added@epam.com";
        val nonexistentEmail1 = "no_such_user_in_telescope@epam.com";
        val nonexistentEmail2 = SOME_NOT_USER_EMAIL.toLowerCase();

        doReturn(Lists.newArrayList(someTelescopeEmployeeEntityDtoWithEmail(existentEmail))).when(telescopeService)
                .getUsersInfoByEmails(anySetOf(String.class));
        doReturn(someUser()).when(userAggregator)
                .userTelescopeInfoDtoToUser(any(), eq(existentEmail), any());

        val someEmails = Arrays.asList(existentEmail, nonexistentEmail1, nonexistentEmail2);
        val actual = userService.addIfNotExistAllWithRole(someEmails, ROLE_SIMPLE_USER);

        assertEquals(1, actual);
        verify(telescopeService, times(1)).getUsersInfoByEmails(anySetOf(String.class));
    }

    @Test
    public void addIfNotExistAllWithRoleSimpleUserAddedInDistributionList() {
        val userWithSimpleRoleEmail = "steven_tyler@epam.com";

        doReturn(Lists.newArrayList(someTelescopeEmployeeEntityDtoWithEmail(userWithSimpleRoleEmail))).when(telescopeService)
                .getUsersInfoByEmails(anySetOf(String.class));

        userService.addIfNotExistAllWithRole(Arrays.asList(userWithSimpleRoleEmail), ROLE_EXTENDED_USER);

        assertThat(userService.findUserByEmailOrThrowException(userWithSimpleRoleEmail))
                .extracting("roles", "isActive")
                .containsOnly(Sets.immutableEnumSet(ROLE_EXTENDED_USER), true);
    }

    @Test
    public void addIfNotExistAllWithExDistributionListUserChangeRoleToSimpleUser() {
        val exDistributionListUserEmail = "tyler_greeds@epam.com";

        doReturn(Lists.newArrayList(someTelescopeEmployeeEntityDtoWithEmail(exDistributionListUserEmail))).when(telescopeService)
                .getUsersInfoByEmails(anySetOf(String.class));

        userService.addIfNotExistAllWithRole(Arrays.asList(exDistributionListUserEmail), ROLE_SIMPLE_USER);

        assertThat(userService.findUserByEmailOrThrowException(exDistributionListUserEmail))
                .extracting("roles", "isActive")
                .containsOnly(Sets.immutableEnumSet(ROLE_SIMPLE_USER, ROLE_ADMIN), false);
    }

    @Test(expected = NoSuchUserException.class)
    public void getUserByEmailWithNonExistingEmail() {
        userService.findUserByEmailOrThrowException(SOME_NOT_USER_EMAIL);
    }

    @Test(expected = NoSuchUserException.class)
    public void getUserByIdWithNonExistingId() {
        userService.findUserById(NON_EXISTING_USER_ID);
    }

    @Test
    public void findAllActive() {
        val allowedEmails = Arrays.asList("Tyler_Derden@mylo.com", "Ernest_Hemingway@epam.com", "Donald_Gardner@epam.com",
                "Bob_Hoplins@epam.com", "John_Doe@epam.com");

        assertThat(userService.findAllActive())
                .hasSize(4)
                .allSatisfy(user -> assertThat(allowedEmails).contains(user.getEmail()));
    }

    @Test
    public void existsActiveUserWithExtendedUserRoleAndEmail() {
        assertThat(userService.existsActiveUserWithRoleAndEmail(ROLE_EXTENDED_USER, "John_Doe@epam.com")).isTrue();
        assertThat(userService.existsActiveUserWithRoleAndEmail(ROLE_EXTENDED_USER, "john_doe@epam.com")).isTrue();
        assertThat(userService.existsActiveUserWithRoleAndEmail(ROLE_EXTENDED_USER, "bob_hoplins@epam.com")).isTrue();
        assertThat(userService.existsActiveUserWithRoleAndEmail(ROLE_EXTENDED_USER, "tyler_greeds@epam.com")).isFalse();
        assertThat(userService.existsActiveUserWithRoleAndEmail(ROLE_EXTENDED_USER, "Steven_Tyler@epam.com")).isFalse();
        assertThat(userService.existsActiveUserWithRoleAndEmail(ROLE_EXTENDED_USER, "Donald_Gardner@epam.com")).isFalse();
        assertThat(userService.existsActiveUserWithRoleAndEmail(ROLE_EXTENDED_USER, "no_such_email@epam.com")).isFalse();
    }

    @Test
    public void existsActiveUserWithAdminRoleAndEmail() {
        assertThat(userService.existsActiveUserWithRoleAndEmail(ROLE_ADMIN, "John_Doe@epam.com")).isTrue();
        assertThat(userService.existsActiveUserWithRoleAndEmail(ROLE_ADMIN, "john_doe@epam.com")).isTrue();
        assertThat(userService.existsActiveUserWithRoleAndEmail(ROLE_ADMIN, "ernest_hemingway@epam.com")).isFalse();
        assertThat(userService.existsActiveUserWithRoleAndEmail(ROLE_ADMIN, "Tyler_Greeds@epam.com")).isFalse();
        assertThat(userService.existsActiveUserWithRoleAndEmail(ROLE_ADMIN, "Donald_Gardner@epam.com")).isFalse();
        assertThat(userService.existsActiveUserWithRoleAndEmail(ROLE_ADMIN, "no_such_email@epam.com")).isFalse();
    }

    @Test
    public void existsActiveUserWithSimpleUserRoleAndEmail() {
        assertThat(userService.existsActiveUserWithRoleAndEmail(ROLE_SIMPLE_USER, "ernest_hemingway@epam.com")).isFalse();
        assertThat(userService.existsActiveUserWithRoleAndEmail(ROLE_SIMPLE_USER, "Ernest_Hemingway@epam.com")).isFalse();
        assertThat(userService.existsActiveUserWithRoleAndEmail(ROLE_SIMPLE_USER, "John_Doe@epam.com")).isFalse();
        assertThat(userService.existsActiveUserWithRoleAndEmail(ROLE_SIMPLE_USER, "Tyler_Greeds@epam.com")).isFalse();
        assertThat(userService.existsActiveUserWithRoleAndEmail(ROLE_SIMPLE_USER, "Donald_Gardner@epam.com")).isFalse();
        assertThat(userService.existsActiveUserWithRoleAndEmail(ROLE_SIMPLE_USER, "no_such_email@epam.com")).isFalse();
    }

    @Test
    public void existsActiveUserWithNotAllowedUserRoleAndEmail() {
        assertThat(userService.existsActiveUserWithRoleAndEmail(ROLE_NOT_ALLOWED_USER, "Donald_Gardner@epam.com")).isTrue();
        assertThat(userService.existsActiveUserWithRoleAndEmail(ROLE_NOT_ALLOWED_USER, "donald_gardner@epam.com")).isTrue();
        assertThat(userService.existsActiveUserWithRoleAndEmail(ROLE_NOT_ALLOWED_USER, "Ernest_Hemingway@epam.com")).isFalse();
        assertThat(userService.existsActiveUserWithRoleAndEmail(ROLE_NOT_ALLOWED_USER, "John_Doe@epam.com")).isFalse();
        assertThat(userService.existsActiveUserWithRoleAndEmail(ROLE_NOT_ALLOWED_USER, "Tyler_Greeds@epam.com")).isFalse();
        assertThat(userService.existsActiveUserWithRoleAndEmail(ROLE_NOT_ALLOWED_USER, "no_such_email@epam.com")).isFalse();
    }

    @Test
    public void findAllUserBaseDtos() {
        val dtoList = someUserBaseDtos();

        doReturn(dtoList).when(userAggregator).usersToListOfBaseDtos(anyListOf(User.class));
        assertThat(userService.findAllUserBaseDtos())
                .hasSize(dtoList.size())
                .allMatch(Objects::nonNull);
    }
}
