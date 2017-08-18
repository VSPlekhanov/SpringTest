package com.epam.lstrsum.service;

import com.epam.lstrsum.service.impl.ElasticSearchServiceImpl;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static com.epam.lstrsum.testutils.InstantiateUtil.someEntity;
import static com.epam.lstrsum.testutils.InstantiateUtil.someInt;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.anyMapOf;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

public class ElasticSearchServiceTest {
    @Mock
    private RestClient restClient;

    @Mock
    private Response response;

    @InjectMocks
    private ElasticSearchServiceImpl elasticSearchService;

    @Before
    public void setUp() {
        initMocks(this);
    }

    @Test
    public void smartSearch() throws Exception {
        doReturn(response).when(restClient).performRequest(anyString(), any(), any(), any(), anyVararg());
        doReturn(someEntity()).when(response).getEntity();

        assertThat(elasticSearchService.smartSearch("one", someInt(), someInt())).isNotNull();

        verify(restClient, times(1))
                .performRequest(eq("GET"), any(), anyMapOf(String.class, String.class), any(), anyVararg());
    }
}
