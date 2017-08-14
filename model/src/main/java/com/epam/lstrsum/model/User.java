package com.epam.lstrsum.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Builder
@Document(collection = User.USER_COLLECTION_NAME)
public class User {
    public final static String USER_COLLECTION_NAME = "User";

    @Id
    private String userId;
    private String firstName;
    private String lastName;
    private String email;
    private String[] roles;
    private Instant createdAt;
    private Boolean isActive;
}
