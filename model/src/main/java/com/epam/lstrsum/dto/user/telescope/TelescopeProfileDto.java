package com.epam.lstrsum.dto.user.telescope;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder
public class TelescopeProfileDto {
    private String origin;
    private String id;
    private String status;
    private String url;
    private String visibility;
}
