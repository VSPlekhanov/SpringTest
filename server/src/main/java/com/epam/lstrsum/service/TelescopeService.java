package com.epam.lstrsum.service;

import com.epam.lstrsum.dto.user.telescope.TelescopeEmployeeEntityDto;

public interface TelescopeService {
    String TELESCOPE_API_FTS_SEARCH_URL = "https://telescope.epam.com/eco/rest/e3s-eco-scripting-impl/0.1.0/data/searchFts";
    String TELESCOPE_API_PHOTO_URL = "https://telescope.epam.com/rest/logo/v1/logo";
    String TELESCOPE_API_META_TYPE_FIELD_VALUE = "meta:people-suite:people-api:com.epam.e3s.app.people.api.data.pluggable.EmployeeEntity";

    String TELESCOPE_API_SEARCH_QUERY_FULLNAME = "{\"statements\":[{\"query\":\"fullName:(";
    String TELESCOPE_API_SEARCH_QUERY_FILTERS = ")\"}],\"filters\":[{\"field\":\"employmentStatus\",\"values\":[\"Employee\",\"Contractor\",\"Intern\",\"Trainee\"]}],\"limit\":";
    String TELESCOPE_API_SEARCH_QUERY_SORTING = ",\"sorting\":[{\"fullName\":1}]}";
    String TELESCOPE_API_FIELDS_FOR_UI = "_e3sId,email,fullName,displayName,primarySkill,primaryTitle,manager,profile,photo,unitPath";

    String TELESCOPE_API_EMAIL_SEARCH_EMAIL = "{\"statements\":[{\"query\":\"email:(";
    String TELESCOPE_API_EMAIL_SEARCH_FILTERS = ")\"}],\"filters\":[{\"field\":\"employmentStatus\",\"values\":[\"Employee\",\"Contractor\",\"Intern\",\"Trainee\"]}]}";
    String TELESCOPE_API_FIELDS_FOR_ADD_NEW_USER = "firstName,lastName";

    String EMAIL_EPAM_DOMAIN = "@epam.com";

    TelescopeEmployeeEntityDto[] getUserInfoByFullName(String fullName, Integer limit);

    String getUserPhotoByUri(String uri);

    TelescopeEmployeeEntityDto[] getUserInfoByEmail(String email);
}
