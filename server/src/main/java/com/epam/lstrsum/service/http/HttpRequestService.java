package com.epam.lstrsum.service.http;

import com.epam.lstrsum.utils.HttpUtilEntity;
import org.springframework.core.ParameterizedTypeReference;

public interface HttpRequestService {
    <T> T sendGetRequest(HttpUtilEntity httpUtilEntity, ParameterizedTypeReference<T> type);

    default <T> T sendGetRequest(HttpUtilEntity httpUtilEntity, Class<T> type) {
        return sendGetRequest(httpUtilEntity, new ParameterizedTypeReference<T>() {
        });
    }
}
