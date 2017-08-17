package com.epam.lstrsum.service;

import com.epam.lstrsum.dto.user.telescope.TelescopeEmployeeEntityDto;
import com.epam.lstrsum.exception.NoSuchUserException;
import com.epam.lstrsum.service.http.HttpRequestService;
import com.epam.lstrsum.service.impl.TelescopeServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static com.epam.lstrsum.testutils.InstantiateUtil.SOME_NOT_USER_EMAIL;
import static com.epam.lstrsum.testutils.InstantiateUtil.SOME_USER_EMAIL;
import static com.epam.lstrsum.testutils.InstantiateUtil.someTelescopeEmployeeEntityDto;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.MockitoAnnotations.initMocks;

public class TelescopeServiceTest {
    static private final int SOME_VALID_LIMIT = 10;
    static private final int SOME_NOT_VALID_LIMIT = 10000;
    static private final String SOME_URI = "some uri";

    @Mock
    private HttpRequestService httpRequestService;

    private TelescopeService telescopeService;

    @Before
    public void setUp() {
        initMocks(this);

        telescopeService = new TelescopeServiceImpl(httpRequestService);
    }

    @Test
    public void getUserInfoByFullName() {
        final TelescopeEmployeeEntityDto dto = someTelescopeEmployeeEntityDto();
        doReturn(new TelescopeEmployeeEntityDto[]{dto}).when(httpRequestService).sendGETRequest(any(), any());

        TelescopeEmployeeEntityDto[] actualResponse = telescopeService.getUsersInfoByFullName("name", SOME_VALID_LIMIT);

        assertThat(actualResponse)
                .hasOnlyOneElementSatisfying(e -> assertThat(e).isEqualToComparingFieldByFieldRecursively(dto));
    }

    @Test
    public void getUserInfoByFullNameWithEmptyFullName() {
        assertThat(telescopeService.getUsersInfoByFullName("    ", SOME_VALID_LIMIT)).hasSize(0);
    }

    @Test
    public void getUserInfoByFullNameWithOverMaxLimitValue() {
        assertThat(telescopeService.getUsersInfoByFullName("    ", SOME_NOT_VALID_LIMIT)).hasSize(0);
    }

    @Test
    public void getUserPhotoByUri() {
        assertThat(telescopeService.getUserPhotoByUri(SOME_URI)).isNotEmpty();
    }

    @Test(expected = NoSuchUserException.class)
    public void getUserInfoByEmailWithIncorrectDomainEmail() {
        telescopeService.getUserInfoByEmail(SOME_NOT_USER_EMAIL);
    }

    @Test
    public void getUserInfoByEmail() {
        final TelescopeEmployeeEntityDto dto = someTelescopeEmployeeEntityDto();

        doReturn(new TelescopeEmployeeEntityDto[]{dto}).when(httpRequestService).sendGETRequest(any(), any());

        assertThat(telescopeService.getUserInfoByEmail(SOME_USER_EMAIL)).isEqualToComparingFieldByFieldRecursively(dto);
    }
}
