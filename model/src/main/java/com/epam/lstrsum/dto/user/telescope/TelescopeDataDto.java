package com.epam.lstrsum.dto.user.telescope;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@AllArgsConstructor
@Getter
@Builder
public class TelescopeDataDto {

    private String _e3sId;
    private String[] email;
    private String[] fullName;
    private String firstName;
    private String lastName;
    private String displayName;
    private String primarySkill;
    private String primaryTitle;
    private String manager;
    private Map<String, TelescopeProfileDto[]> profile;
    private String[] photo;
    private String unitPath;
}
