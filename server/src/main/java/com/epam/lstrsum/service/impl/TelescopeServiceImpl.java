package com.epam.lstrsum.service.impl;

import com.epam.lstrsum.dto.user.telescope.TelescopeEmployeeEntityDto;
import com.epam.lstrsum.service.TelescopeService;
import com.epam.lstrsum.service.http.HttpRequestService;
import com.epam.lstrsum.utils.HttpUtilEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

@Service
@Slf4j
@RequiredArgsConstructor
public class TelescopeServiceImpl implements TelescopeService {
    private final HttpRequestService httpRequestService;

    @Value("${email.from-address}")
    private String telescopeUsername;

    @Value("${email.password}")
    private String telescopePassword;

    @Override
    public TelescopeEmployeeEntityDto[] getUsersInfoByFullName(String fullName, int limit) {
        if (!isDataForUserSearchValid(fullName, limit)) {
            log.warn("No request was sent to telescope api because of invalid parameters");
            return new TelescopeEmployeeEntityDto[]{};
        }

        return httpRequestService.sendGETRequest(
                getEntityTemplate()
                        .parametersValues(
                                Arrays.asList(TELESCOPE_API_META_TYPE_FIELD_VALUE,
                                        TELESCOPE_API_SEARCH_QUERY_FULLNAME + fullName + TELESCOPE_API_SEARCH_QUERY_FILTERS +
                                                limit + TELESCOPE_API_SEARCH_QUERY_SORTING,
                                        TELESCOPE_API_FIELDS_FOR_UI)
                        ).build(),
                TelescopeEmployeeEntityDto[].class
        );
    }

    @Override
    public String getUserPhotoByUri(String uri) {
        return String.format("%s?uri=%s", TELESCOPE_API_PHOTO_URL, uri);
    }

    @Override
    public List<TelescopeEmployeeEntityDto> getUsersInfoByEmails(Set<String> emails) {
        log.debug("getUsersInfoByEmails.enter; emails size for search into telescope: {}", isNull(emails) ? "null list" : emails.size());
        String emailsForSearch = prepareEmailsForSearch(emails);
        return Arrays.asList(httpRequestService.sendGETRequest(
                getEntityTemplate()
                        .parametersValues(
                                Arrays.asList(TELESCOPE_API_META_TYPE_FIELD_VALUE,
                                        TELESCOPE_API_EMAIL_SEARCH_QUERY + TELESCOPE_API_EMAIL_SEARCH_FILTER_EMAIL + emailsForSearch +
                                                TELESCOPE_API_EMAIL_SEARCH_FILTER_EMPLOYMENT_STATUS,
                                        TELESCOPE_API_FIELDS_FOR_ADD_NEW_USER))
                        .build(),
                TelescopeEmployeeEntityDto[].class));
    }

    private String prepareEmailsForSearch(Set<String> emails) {
        return emails.stream().map(e -> "\"" + e + "\"").collect(Collectors.joining(","));
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