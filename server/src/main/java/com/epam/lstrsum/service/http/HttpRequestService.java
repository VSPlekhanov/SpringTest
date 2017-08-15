package com.epam.lstrsum.service.http;

import com.epam.lstrsum.utils.HttpUtilEntity;

public interface HttpRequestService {
    <T> T sendGETRequest(HttpUtilEntity httpUtilEntity, Class<T> type);
}
