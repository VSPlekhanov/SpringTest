package com.epam.lstrsum.service;

import com.epam.lstrsum.dto.user.telescope.TelescopeEmployeeEntityDto;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public interface TelescopeService {
    String TELESCOPE_API_FTS_SEARCH_URL = "https://telescope.epam.com/eco/rest/e3s-eco-scripting-impl/0.1.0/data/searchFts";
    String TELESCOPE_API_PHOTO_URL = "https://telescope.epam.com/rest/logo/v1/logo?uri=%s";
    String TELESCOPE_API_META_TYPE_FIELD_VALUE = "meta:people-suite:people-api:com.epam.e3s.app.people.api.data.pluggable.EmployeeEntity";

    String TELESCOPE_API_SEARCH_QUERY_FULLNAME = "{\"statements\":[{\"query\":\"fullName:(";
    String TELESCOPE_API_SEARCH_QUERY_FILTERS =
            ")\"}],\"filters\":[{\"field\":\"employmentStatus\",\"values\":[\"Employee\",\"Contractor\",\"Intern\",\"Trainee\"]}],\"limit\":";
    String TELESCOPE_API_SEARCH_QUERY_SORTING = ",\"sorting\":[{\"fullName\":1}]}";
    String TELESCOPE_API_FIELDS_FOR_UI = "email,fullName,displayName,primarySkill,primaryTitle,manager,profile,photo,unitPath";

    String TELESCOPE_API_EMAIL_SEARCH_QUERY = "{\"statements\":[{\"query\":\"*\"}],";
    String TELESCOPE_API_EMAIL_SEARCH_FILTER_EMAIL = "\"filters\":[{\"field\":\"email\",\"values\":[";
    String TELESCOPE_API_EMAIL_SEARCH_FILTER_EMPLOYMENT_STATUS =
            "]},{\"field\":\"employmentStatus\",\"values\":[\"Employee\",\"Contractor\",\"Intern\",\"Trainee\"]}]}";
    String TELESCOPE_API_FIELDS_FOR_ADD_NEW_USER = "email,firstName,lastName";

    List<String> SEARCH_QUERY_PARAMETERS_NAMES = Arrays.asList("metaType", "query", "fields");

    /**
     * Performs a request to telescope api to get users info.
     *
     * @param fullName String name/part name for users elastic search into telescope
     * @param limit    int value with max users amount in response
     * @return List with users info from telescope
     */
    List<TelescopeEmployeeEntityDto> getUsersInfoByFullName(String fullName, int limit);

    /**
     * Retrieve photo in base64 from telescope.
     *
     * @param uri format example "attachment:///upsa_profilePhoto.4060741400007345041_1.GIF_cba0891d-a69f-47c9-96ib-c61a14e6e33d"
     * @return string with base64
     */
    byte[] getUserPhotoByUri(String uri);

    List<TelescopeEmployeeEntityDto> getUsersInfoByEmails(Set<String> emails);

    default List<TelescopeEmployeeEntityDto> getUsersInfoByEmails(List<String> emails) {
        return getUsersInfoByEmails(new HashSet<>(emails));
    }
}
