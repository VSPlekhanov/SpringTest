package com.epam.lstrsum.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class UserPostDto {
    private String firstName;
    private String lastName;
    private String email;
    private String[] roles;
}
