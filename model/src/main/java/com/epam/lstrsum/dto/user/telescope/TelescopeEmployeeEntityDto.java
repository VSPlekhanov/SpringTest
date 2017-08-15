package com.epam.lstrsum.dto.user.telescope;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class TelescopeEmployeeEntityDto {
    private TelescopeDataDto data;
    private String[] sortValues;
}