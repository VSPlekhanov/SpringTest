package com.epam.lstrsum.utils;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@Getter
@Builder
public class HttpUtilEntity {
    private String username;
    private String password;
    private String url;
    private List<String> parametersNames;
    private List<String> parametersValues;
    private List<String> headersNames;
    private List<String> headersValues;
}
