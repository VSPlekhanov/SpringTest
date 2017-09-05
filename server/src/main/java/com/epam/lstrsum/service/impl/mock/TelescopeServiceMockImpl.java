package com.epam.lstrsum.service.impl.mock;

import com.epam.lstrsum.dto.user.telescope.TelescopeDataDto;
import com.epam.lstrsum.dto.user.telescope.TelescopeEmployeeEntityDto;
import com.epam.lstrsum.dto.user.telescope.TelescopeProfileDto;
import com.epam.lstrsum.service.TelescopeService;
import com.epam.lstrsum.utils.FunctionalUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.epam.lstrsum.utils.FunctionalUtil.getEmailPostfix;
import static com.epam.lstrsum.utils.FunctionalUtil.getList;
import static com.epam.lstrsum.utils.FunctionalUtil.getMap;
import static com.epam.lstrsum.utils.FunctionalUtil.getRandomString;

@Service
@Profile("standalone")
@Primary
@RequiredArgsConstructor
public class TelescopeServiceMockImpl implements TelescopeService {
    private final CounterService counterService;

    private String base64;

    @PostConstruct
    public void init() throws IOException {
        final InputStream base64Resource = getClass().getResourceAsStream("/data/base64");
        base64 = IOUtils.toString(base64Resource, "UTF-8");
    }

    @Override
    public List<TelescopeEmployeeEntityDto> getUsersInfoByFullName(String fullName, int limit) {
        counterService.increment("telescope.get.users.info.by.full.name");
        return getList(() -> telescopeEmployeeEntityDtoByFullName(fullName), limit);
    }

    @Override
    public String getUserPhotoByUri(String uri) {
        counterService.increment("telescope.get.photo");
        return base64;
    }

    @Override
    public List<TelescopeEmployeeEntityDto> getUsersInfoByEmails(Set<String> emails) {
        counterService.increment("telescope.get.users.info.by.emails");
        return emails.stream()
                .map(this::telescopeEmployeeEntityDtoByEmail)
                .collect(Collectors.toList());
    }

    private TelescopeEmployeeEntityDto telescopeEmployeeEntityDtoByEmail(String email) {
        return TelescopeEmployeeEntityDto.builder()
                .data(telescopeDataDtoByEmail(email))
                .sortValues(Collections.emptyList())
                .build();
    }

    private TelescopeDataDto telescopeDataDtoByEmail(String email) {
        final int index = email.indexOf("@");
        final String fullName;
        if (index < 0) {
            fullName = getRandomString();
        } else {
            fullName = email.substring(0, index);
        }

        return TelescopeDataDto.builder()
                ._e3sId(getRandomString())
                .email(getList(() -> email))
                .fullName(getList(() -> fullName))
                .firstName(getRandomString())
                .lastName(getRandomString())
                .displayName(getRandomString())
                .primarySkill(getRandomString())
                .primaryTitle(getRandomString())
                .manager(getRandomString())
                .profile(profile())
                .photo(getList(FunctionalUtil::getRandomString))
                .unitPath(getRandomString())
                .build();
    }

    private TelescopeEmployeeEntityDto telescopeEmployeeEntityDtoByFullName(String fullName) {
        return TelescopeEmployeeEntityDto.builder()
                .data(telescopeDataDtoByFullName(fullName))
                .sortValues(Collections.emptyList())
                .build();
    }

    private TelescopeDataDto telescopeDataDtoByFullName(String fullName) {
        return TelescopeDataDto.builder()
                ._e3sId(getRandomString())
                .email(getList(() -> fullName + getEmailPostfix()))
                .fullName(getList(() -> fullName + getRandomString()))
                .firstName(getRandomString())
                .lastName(getRandomString())
                .displayName(getRandomString())
                .primarySkill(getRandomString())
                .primaryTitle(getRandomString())
                .manager(getRandomString())
                .profile(profile())
                .photo(getList(FunctionalUtil::getRandomString))
                .unitPath(getRandomString())
                .build();
    }

    private Map<String, List<TelescopeProfileDto>> profile() {
        return getMap(
                FunctionalUtil::getRandomString,
                () -> getList(this::telescopeProfileDto)
        );
    }

    private TelescopeProfileDto telescopeProfileDto() {
        return TelescopeProfileDto.builder()
                .id(getRandomString())
                .origin(getRandomString())
                .status(getRandomString())
                .url(getRandomString())
                .visibility(getRandomString())
                .build();
    }
}
