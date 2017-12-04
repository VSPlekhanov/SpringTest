package com.epam.lstrsum.service.http;

import com.epam.lstrsum.exception.BusinessLogicException;
import com.epam.lstrsum.utils.HttpUtilEntity;
import com.epam.lstrsum.utils.MessagesHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.tomcat.util.codec.binary.Base64;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

import static java.util.Objects.isNull;

@Service
@Slf4j
@RequiredArgsConstructor
public class HttpRequestServiceImpl implements HttpRequestService {

    private final RestTemplate restTemplate;

    @Autowired
    private final MessagesHelper messagesHelper;

    public <T> T sendGetRequest(HttpUtilEntity httpUtilEntity, ParameterizedTypeReference<T> type) {
        HttpHeaders httpHeaders = new HttpHeaders();
        if (!isCredentialsNull(httpUtilEntity.getUsername(), httpUtilEntity.getPassword())) {
            httpHeaders = addIfPresentBasicAuthorizationHeader(httpHeaders, httpUtilEntity.getUsername(), httpUtilEntity.getPassword());
        }
        if (!isHeadersNull(httpUtilEntity.getHeadersNames(), httpUtilEntity.getHeadersValues())) {
            httpHeaders = addIfPresentHeaders(httpHeaders, httpUtilEntity.getHeadersNames(), httpUtilEntity.getHeadersValues());
        }

        checkUrl(httpUtilEntity.getUrl());
        String url = httpUtilEntity.getUrl();
        if (!isUrlParametersNull(httpUtilEntity.getParametersNames(), httpUtilEntity.getParametersValues())) {
            url += addIfPresentParameters(httpUtilEntity.getParametersNames(), httpUtilEntity.getParametersValues());
        }

        return executeGETRequest(url, new HttpEntity(httpHeaders), type);
    }

    private boolean isUrlParametersNull(List<String> parametersNames, List<String> parametersValues) {
        return isNull(parametersNames) || isNull(parametersValues);
    }

    private void checkUrl(String url) {
        if (isNull(url) || url.trim().isEmpty()) {
            log.error("Cannot sent a http request to null or empty url");
            throw new BusinessLogicException(messagesHelper.get("validation.service.cannot-sent-http-request-to-null-or-empty-url"));
        }
    }

    private boolean isHeadersNull(List<String> headersNames, List<String> headersValues) {
        return isNull(headersNames) || isNull(headersValues);
    }

    private boolean isCredentialsNull(String username, String password) {
        return isNull(username) || isNull(password);
    }

    private String addIfPresentParameters(List<String> parametersNames, List<String> parametersValues) {
        if (!parametersNames.isEmpty() && !parametersValues.isEmpty()) {
            if (parametersNames.size() == parametersValues.size()) {
                log.debug("Add parameters to GET request url");
                StringJoiner joiner = new StringJoiner("&");
                for (int i = 0; i < parametersNames.size(); i++) {
                    joiner.add(parametersNames.get(i) + "=" + parametersValues.get(i));
                }
                return "?" + joiner;
            } else {
                log.warn(
                        "Parameters won't be added to GET request URL because of different names and values size. Names size = {}, values size = {}",
                        parametersNames, parametersValues);
            }
        }
        return "";
    }

    private HttpHeaders addIfPresentHeaders(HttpHeaders httpHeaders, List<String> headerNames, List<String> headerValues) {
        if (!headerNames.isEmpty() && !headerValues.isEmpty()) {
            if (headerNames.size() == headerValues.size()) {
                log.debug("Add headers to request");
                getHeadersMap(headerNames, headerValues).forEach(httpHeaders::set);
            } else {
                log.warn("Headers won't be added to GET request because of different size. Names size = {}, values size = {}",
                        headerNames.size(), headerValues.size());
            }
        }
        return httpHeaders;
    }

    private HttpHeaders addIfPresentBasicAuthorizationHeader(HttpHeaders httpHeaders, String username, String password) {
        if (!username.trim().isEmpty() && !password.trim().isEmpty()) {
            log.debug("Add authorization header to request");
            httpHeaders.set(HttpHeaders.AUTHORIZATION, "Basic " + createAuthHeader(username, password));
        } else {
            log.warn("Try to add empty username and password to AUTHORIZATION httpHeader");
        }
        return httpHeaders;
    }

    private String createAuthHeader(final String username, final String password) {
        val auth = username + ":" + password;
        val encodedAuth = Base64.encodeBase64(
                auth.getBytes(Charset.forName("US-ASCII")));
        return new String(encodedAuth);
    }

    private Map<String, String> getHeadersMap(List<String> names, List<String> values) {
        Map<String, String> headers = new HashMap<>();
        for (int i = 0; i < names.size(); i++) {
            headers.put(names.get(i), values.get(i));
        }
        return headers;
    }

    private <T> T executeGETRequest(final String url, final HttpEntity entity, final ParameterizedTypeReference<T> type) {
        try {
            val uri = UriComponentsBuilder.fromUriString(url).build().encode().toUri();
            val out = restTemplate.exchange(uri, HttpMethod.GET, entity, type);
            if (!Objects.equals(out.getStatusCode(), HttpStatus.OK)) {
                log.error("Incorrect response status code = {} instead of code = 200", out.getStatusCode().toString());
                throw new BusinessLogicException(
                        MessageFormat.format(messagesHelper.get("validation.service.incorrect-responce-status-code"),
                                                out.getStatusCode().toString()));
            }
            return out.getBody();
        } catch (final Exception e) {
            log.error("Can't GET to: {} because of {}", url, e.getMessage());
            throw new BusinessLogicException(
                    MessageFormat.format(messagesHelper.get("validation.service.cannot-perform-get-request-because-of"),
                                            e.getMessage()));
        }
    }
}
