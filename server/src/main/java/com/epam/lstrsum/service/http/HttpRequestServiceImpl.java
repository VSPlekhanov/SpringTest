package com.epam.lstrsum.service.http;

import com.epam.lstrsum.exception.BusinessLogicException;
import com.epam.lstrsum.utils.HttpUtilEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.Charset;
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

    public <T> T sendGETRequest(HttpUtilEntity httpUtilEntity, Class<T> type) {
        HttpHeaders httpHeaders = new HttpHeaders();
        if (!isCredentialsNull(httpUtilEntity.getUsername(), httpUtilEntity.getPassword())) {
            httpHeaders = addIfPresentBasicAuthorizationHeader(httpHeaders, httpUtilEntity.getUsername(), httpUtilEntity.getPassword());
        }
        if (!isHeadersNull(httpUtilEntity.getHeadersNames(), httpUtilEntity.getHeadersValues())) {
            httpHeaders = addIfPresentHeaders(httpHeaders, httpUtilEntity.getHeadersNames(), httpUtilEntity.getHeadersValues());
        }

        if (!isUrlValid(httpUtilEntity.getUrl())) {
            log.error("Cannot sent GET request to null or empty url");
            throw new BusinessLogicException("Cannot sent GET request to null or empty url");
        }

        String url = httpUtilEntity.getUrl();
        if (!isUrlParametersNull(httpUtilEntity.getParametersNames(), httpUtilEntity.getParametersValues())) {
            url += addIfPresentParameters(httpUtilEntity.getParametersNames(), httpUtilEntity.getParametersValues());
        }

        return executeGETRequest(url, new HttpEntity(httpHeaders), type);
    }

    private boolean isUrlParametersNull(List<String> parametersNames, List<String> parametersValues) {
        return isNull(parametersNames) || isNull(parametersValues);
    }

    private boolean isUrlValid(String url) {
        return !isNull(url) && !url.trim().isEmpty();
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
        final String auth = username + ":" + password;
        final byte[] encodedAuth = Base64.encodeBase64(
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

    private <T> T executeGETRequest(final String url, final HttpEntity entity, final Class<T> type) {
        try {
            final URI uri = UriComponentsBuilder.fromUriString(url).build().encode().toUri();
            final ResponseEntity<T> out = restTemplate.exchange(uri, HttpMethod.GET, entity, type);
            if (!Objects.equals(out.getStatusCode(), HttpStatus.OK)) {
                log.warn("Incorrect response status code = {} instead of code = 200", out.getStatusCode().toString());
                throw new BusinessLogicException(
                        "Incorrect response status code = " + out.getStatusCode().toString() + " instead of code = 200");
            }
            return out.getBody();
        } catch (final Exception e) {
            log.warn("Can't GET to: {} because of {}", url, e.getMessage());
            throw new BusinessLogicException("Can't perform GET request because of " + e.getMessage());
        }
    }
}
