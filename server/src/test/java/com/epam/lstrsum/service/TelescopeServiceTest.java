package com.epam.lstrsum.service;

import com.epam.lstrsum.dto.user.telescope.TelescopeDataDto;
import com.epam.lstrsum.dto.user.telescope.TelescopeEmployeeEntityDto;
import com.epam.lstrsum.service.http.HttpRequestService;
import com.epam.lstrsum.utils.HttpUtilEntity;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static com.epam.lstrsum.InstantiateUtil.someTelescopeEmployeeEntityDto;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class TelescopeServiceTest {

    private TelescopeService telescopeService;

    @Mock
    private HttpRequestService httpRequestService;

    @Before
    public void setUp() {
        initMocks(this);

        telescopeService = new TelescopeService(httpRequestService);
    }

    @Test
    public void getUserInfoByFullName() {
        final TelescopeEmployeeEntityDto dto = someTelescopeEmployeeEntityDto();

        when(httpRequestService.sendGETRequest(any(HttpUtilEntity.class), eq(TelescopeEmployeeEntityDto[].class))).thenReturn(new TelescopeEmployeeEntityDto[]{dto});

        TelescopeEmployeeEntityDto[] responseDto = telescopeService.getUserInfoByFullName("name", 10);
        TelescopeDataDto dataDto = responseDto[0].getData();

        assertNotNull(responseDto);
        assertThat(responseDto.length, is(1));
        assertThat(dataDto.getEmail()[0], is("Ivan_Ivanov@epam.com"));

        verify(httpRequestService, times(1)).sendGETRequest(any(HttpUtilEntity.class), eq(TelescopeEmployeeEntityDto[].class));
    }

    @Test
    public void getUserInfoByFullNameWithEmptyFullName() {
        TelescopeEmployeeEntityDto[] employeeEntityDto = telescopeService.getUserInfoByFullName("    ", 10);
        assertThat(employeeEntityDto.length, is(0));
    }

    @Test
    public void getUserInfoByFullNameWithOverMaxLimitValue() {
        TelescopeEmployeeEntityDto[] employeeEntityDto = telescopeService.getUserInfoByFullName("name", 5892);
        assertThat(employeeEntityDto.length, is(0));
    }

    @Test
    public void getUserPhotoByUri() {
        String photoUrl = telescopeService.getUserPhotoByUri("some uri");
        assertNotNull(photoUrl);
        assertThat(photoUrl.length(), greaterThan(0));
    }

    @Test
    public void getUserInfoByEmailWithIncorrectDomainEmail() {
        TelescopeEmployeeEntityDto[] employeeEntityDto = telescopeService.getUserInfoByEmail("email@test.com");
        assertThat(employeeEntityDto.length, is(0));
    }

    @Test
    public void getUserInfoByEmailWithNullEmail() {
        TelescopeEmployeeEntityDto[] employeeEntityDto = telescopeService.getUserInfoByEmail(null);
        assertThat(employeeEntityDto.length, is(0));
    }

    @Test
    public void getUserInfoByEmail() {
        final String email = "Ivan_Ivanov@epam.com";
        final TelescopeEmployeeEntityDto dto = someTelescopeEmployeeEntityDto();

        when(httpRequestService.sendGETRequest(any(HttpUtilEntity.class), eq(TelescopeEmployeeEntityDto[].class))).thenReturn(new TelescopeEmployeeEntityDto[]{dto});

        TelescopeEmployeeEntityDto[] employeeEntityDto = telescopeService.getUserInfoByEmail(email);
        TelescopeDataDto dataDto = employeeEntityDto[0].getData();

        assertNotNull(employeeEntityDto);
        assertThat(employeeEntityDto.length, is(1));
        assertThat(dataDto.getFirstName(), is("Ivan"));
        assertThat(dataDto.getLastName(), is("Ivanov"));

        verify(httpRequestService, times(1)).sendGETRequest(any(HttpUtilEntity.class), eq(TelescopeEmployeeEntityDto[].class));
    }
}
