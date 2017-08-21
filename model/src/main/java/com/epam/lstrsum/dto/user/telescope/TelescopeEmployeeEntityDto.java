package com.epam.lstrsum.dto.user.telescope;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class TelescopeEmployeeEntityDto {
    private TelescopeDataDto data;
    private List<String> sortValues;
}