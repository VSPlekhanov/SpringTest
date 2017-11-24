package com.epam.lstrsum.service;

import com.epam.lstrsum.dto.user.telescope.TelescopeEmployeeEntityDto;
import com.epam.lstrsum.service.http.HttpRequestService;
import com.epam.lstrsum.service.impl.TelescopeServiceImpl;
import com.epam.lstrsum.testutils.InstantiateUtil;
import com.epam.lstrsum.utils.HttpUtilEntity;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.core.ParameterizedTypeReference;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static com.epam.lstrsum.testutils.InstantiateUtil.someStrings;
import static com.epam.lstrsum.testutils.InstantiateUtil.someTelescopeEmployeeEntityDto;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
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
    @Ignore
    public void getPhotoByUri() {
        byte[] expected = new byte[] { (byte)0xe0, 0x4f, (byte)0xd0};
        doReturn(expected).when(httpRequestService).sendGetRequest(any(HttpUtilEntity.class), eq(new ParameterizedTypeReference<String>() {
        }));

        byte[] userPhotoByUri = telescopeService.getUserPhotoByUri(SOME_URI);

        assertThat(userPhotoByUri).isEqualTo(expected);
    }

    @Test
    public void getUserInfoByFullName() {
        final TelescopeEmployeeEntityDto dto = someTelescopeEmployeeEntityDto();
        doReturn(Collections.singletonList(dto)).when(httpRequestService)
                .sendGetRequest(any(), eq(new ParameterizedTypeReference<List<TelescopeEmployeeEntityDto>>() {
                }));

        List<TelescopeEmployeeEntityDto> actualResponse = telescopeService.getUsersInfoByFullName("name", SOME_VALID_LIMIT);

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
    public void getUsersInfoByEmails() {
        List<TelescopeEmployeeEntityDto> dtos = InstantiateUtil.someTelescopeEmployeeEntityDtos();

        doReturn(dtos).when(httpRequestService)
                .sendGetRequest(any(), eq(new ParameterizedTypeReference<List<TelescopeEmployeeEntityDto>>() {
                }));

        assertThat(telescopeService.getUsersInfoByEmails(new HashSet<>(someStrings())))
                .isEqualTo(dtos);
    }
}
