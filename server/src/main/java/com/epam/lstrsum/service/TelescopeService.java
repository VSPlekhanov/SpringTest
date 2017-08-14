package com.epam.lstrsum.service;

import com.epam.lstrsum.dto.user.telescope.TelescopeEmployeeEntityDto;
import com.epam.lstrsum.utils.HttpUtilEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;

import static java.util.Objects.isNull;

@Service
@Slf4j
@RequiredArgsConstructor
public class TelescopeService {

    private final HttpRequestService httpRequestService;

    @Value("${email.from-address}")
    private String TELESCOPE_USERNAME;

    @Value("${email.password}")
    private String TELESCOPE_PASSWORD;

    private final String TELESCOPE_API_FTS_SEARCH_URL = "https://telescope.epam.com/eco/rest/e3s-eco-scripting-impl/0.1.0/data/searchFts";
    private final String TELESCOPE_API_PHOTO_URL = "https://telescope.epam.com/rest/logo/v1/logo";
    private final String TELESCOPE_API_META_TYPE_FIELD_VALUE = "meta:people-suite:people-api:com.epam.e3s.app.people.api.data.pluggable.EmployeeEntity";

    private final String TELESCOPE_API_SEARCH_QUERY_FULLNAME = "{\"statements\":[{\"query\":\"fullName:(";
    private final String TELESCOPE_API_SEARCH_QUERY_FILTERS = ")\"}],\"filters\":[{\"field\":\"employmentStatus\",\"values\":[\"Employee\",\"Contractor\",\"Intern\",\"Trainee\"]}],\"limit\":";
    private final String TELESCOPE_API_SEARCH_QUERY_SORTING = ",\"sorting\":[{\"fullName\":1}]}";
    private final String TELESCOPE_API_FIELDS_FOR_UI = "_e3sId,email,fullName,displayName,primarySkill,primaryTitle,manager,profile,photo,unitPath";

    private final String TELESCOPE_API_EMAIL_SEARCH_EMAIL = "{\"statements\":[{\"query\":\"email:(";
    private final String TELESCOPE_API_EMAIL_SEARCH_FILTERS = ")\"}],\"filters\":[{\"field\":\"employmentStatus\",\"values\":[\"Employee\",\"Contractor\",\"Intern\",\"Trainee\"]}]}";
    private final String TELESCOPE_API_FIELDS_FOR_ADD_NEW_USER = "firstName,lastName";

    private final String EMAIL_EPAM_DOMAIN = "@epam.com";


    public TelescopeEmployeeEntityDto[] getUserInfoByFullName(String fullName, Integer limit) {
        if (!isDataForUserSearchValid(fullName, limit)) {
            log.warn("No request was sent to telescope api because of invalid parameters");
            return new TelescopeEmployeeEntityDto[]{};
        }
        HttpUtilEntity httpUtilEntity = HttpUtilEntity.builder()
                .username(TELESCOPE_USERNAME)
                .password(TELESCOPE_PASSWORD)
                .url(TELESCOPE_API_FTS_SEARCH_URL)
                .parametersNames(Arrays.asList("metaType", "query", "fields"))
                .parametersValues(Arrays.asList(TELESCOPE_API_META_TYPE_FIELD_VALUE,
                        TELESCOPE_API_SEARCH_QUERY_FULLNAME + fullName + TELESCOPE_API_SEARCH_QUERY_FILTERS + limit + TELESCOPE_API_SEARCH_QUERY_SORTING,
                        TELESCOPE_API_FIELDS_FOR_UI))
                .build();

        return httpRequestService.sendGETRequest(httpUtilEntity, TelescopeEmployeeEntityDto[].class);
    }

    private boolean isDataForUserSearchValid(String fullName, Integer limit) {
        if (isNullOrEmptyString(fullName)) {
            log.warn("Field fullName is null or empty");
            return false;
        }
        if (isNull(limit) || limit <= 0 || limit > 5000) {
            log.warn("The limit value must be greater than 0 and less or equals 5000 instead of = {}", limit);
            return false;
        }
        return true;
    }

    public String getUserPhotoByUri(String uri) {
        return TELESCOPE_API_PHOTO_URL + "?uri=" + uri;
    }

    public TelescopeEmployeeEntityDto[] getUserInfoByEmail(String email) {
        if (!isEmailValid(email)) {
            log.warn("No request was sent to telescope api because of invalid email value");
            return new TelescopeEmployeeEntityDto[]{};
        }
        HttpUtilEntity httpUtilEntity = HttpUtilEntity.builder()
                .username(TELESCOPE_USERNAME)
                .password(TELESCOPE_PASSWORD)
                .url(TELESCOPE_API_FTS_SEARCH_URL)
                .parametersNames(Arrays.asList("metaType", "query", "fields"))
                .parametersValues(Arrays.asList(TELESCOPE_API_META_TYPE_FIELD_VALUE,
                        TELESCOPE_API_EMAIL_SEARCH_EMAIL + email + TELESCOPE_API_EMAIL_SEARCH_FILTERS,
                        TELESCOPE_API_FIELDS_FOR_ADD_NEW_USER))
                .build();

        return httpRequestService.sendGETRequest(httpUtilEntity, TelescopeEmployeeEntityDto[].class);
    }

    private boolean isEmailValid(String email) {
        if (isNullOrEmptyString(email)) {
            log.warn("Email value mustn't be null or empty");
            return false;
        }
        if (!isEpamDomain(email)) {
            log.warn("Incorrect email domain value instead of = {}", EMAIL_EPAM_DOMAIN);
            return false;
        }
        return true;
    }

    private boolean isEpamDomain(String email) {
        return email.substring(email.indexOf("@"), email.length()).equals(EMAIL_EPAM_DOMAIN);
    }

    private boolean isNullOrEmptyString(String stringForCheck) {
        return isNull(stringForCheck) || stringForCheck.trim().isEmpty();
    }
}
