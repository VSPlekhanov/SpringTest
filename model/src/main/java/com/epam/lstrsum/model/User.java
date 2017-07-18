package com.epam.lstrsum.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Document(collection = User.USER_COLLECTION_NAME)
public class User {
    public final static String USER_COLLECTION_NAME = "user";

    @Id
    private String userId;
    private String firstName;
    private String lastName;
    private String email;
    private String[] roles;
    private Instant createdAt;
    private Boolean isActive;
}
