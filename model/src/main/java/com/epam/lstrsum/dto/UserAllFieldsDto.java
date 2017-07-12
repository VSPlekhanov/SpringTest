package com.epam.lstrsum.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@AllArgsConstructor
@Getter
public class UserAllFieldsDto {
    private String userId;
    private String firstName;
    private String lastName;
    private String email;
    private String[] roles;
    private Instant createdAt;
    private Boolean isActive;
}
