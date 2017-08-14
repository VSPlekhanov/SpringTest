package com.epam.lstrsum.model;

import com.epam.lstrsum.enums.UserRoleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Document(collection = User.USER_COLLECTION_NAME)
public class User {
    public final static String USER_COLLECTION_NAME = "user";

    @Id
    private String userId;
    private String firstName;
    private String lastName;
    @Indexed(unique = true)
    private String email;
    private List<UserRoleType> roles;
    private Instant createdAt;
    private Boolean isActive;
}
