package com.epam.lstrsum.service.impl;

import com.epam.lstrsum.dto.user.telescope.TelescopeEmployeeEntityDto;
import com.epam.lstrsum.service.TelescopeService;
import com.epam.lstrsum.service.http.HttpRequestService;
import com.epam.lstrsum.utils.HttpUtilEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static org.apache.commons.collections4.CollectionUtils.size;

@Service
@Slf4j
@RequiredArgsConstructor
public class TelescopeServiceImpl implements TelescopeService {
    private final HttpRequestService httpRequestService;

    @Value("${spring.mail.username}")
    private String telescopeUsername;

    @Value("${spring.mail.password}")
    private String telescopePassword;

    private ParameterizedTypeReference<List<TelescopeEmployeeEntityDto>> LIST_TELESCOPE_EMPLOYEE_ENTITIES =
            new ParameterizedTypeReference<List<TelescopeEmployeeEntityDto>>() {
            };

    @Override
    public List<TelescopeEmployeeEntityDto> getUsersInfoByFullName(String fullName, int limit) {
        if (!isDataForUserSearchValid(fullName, limit)) {
            log.warn("No request was sent to telescope api because of invalid parameters");
            return Collections.emptyList();
        }

        return httpRequestService.sendGetRequest(
                getEntityTemplate()
                        .parametersValues(
                                Arrays.asList(TELESCOPE_API_META_TYPE_FIELD_VALUE,
                                        TELESCOPE_API_SEARCH_QUERY_FULLNAME + fullName + TELESCOPE_API_SEARCH_QUERY_FILTERS +
                                                limit + TELESCOPE_API_SEARCH_QUERY_SORTING,
                                        TELESCOPE_API_FIELDS_FOR_UI)
                        ).build(),
                LIST_TELESCOPE_EMPLOYEE_ENTITIES
        );
    }

    @Override
    public String getUserPhotoByUri(String uri) {
        return httpRequestService.sendGetRequest(
                getEntityTemplate()
                        .url(String.format(TELESCOPE_API_PHOTO_URL, uri))
                        .build(),
                new ParameterizedTypeReference<String>() {
                }
        );
    }

    @Override
    public List<TelescopeEmployeeEntityDto> getUsersInfoByEmails(Set<String> emails) {
        log.debug("getUsersInfoByEmails.enter; emails size for search into telescope: {}", emails.size());
        String emailsForSearch = prepareEmailsForSearch(emails);
        return httpRequestService.sendGetRequest(
                getEntityTemplate()
                        .parametersValues(
                                Arrays.asList(TELESCOPE_API_META_TYPE_FIELD_VALUE,
                                        TELESCOPE_API_EMAIL_SEARCH_QUERY + TELESCOPE_API_EMAIL_SEARCH_FILTER_EMAIL + emailsForSearch +
                                                TELESCOPE_API_EMAIL_SEARCH_FILTER_EMPLOYMENT_STATUS + size(emails) + "}",
                                        TELESCOPE_API_FIELDS_FOR_ADD_NEW_USER))
                        .build(),
                LIST_TELESCOPE_EMPLOYEE_ENTITIES);
    }

    private String prepareEmailsForSearch(Set<String> emails) {
        return emails.stream()
                .map(e -> "\"" + e + "\"")
                .map(String::toLowerCase)
                .collect(Collectors.joining(","));
    }

    private HttpUtilEntity.HttpUtilEntityBuilder getEntityTemplate() {
        return HttpUtilEntity.builder()
                .username(telescopeUsername)
                .password(telescopePassword)
                .url(TELESCOPE_API_FTS_SEARCH_URL)
                .parametersNames(SEARCH_QUERY_PARAMETERS_NAMES);
    }

    private boolean isDataForUserSearchValid(String fullName, int limit) {
        return (!isNullOrEmptyString(fullName) && limit > 0 && limit < 5000);
    }

    private boolean isNullOrEmptyString(String stringForCheck) {
        return isNull(stringForCheck) || stringForCheck.trim().isEmpty();
    }
}