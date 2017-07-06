package com.epam.lstrsum.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Document
public class User {
    @Id
    private String userId;
    private String firstName;
    private String lastName;
    private String email;
    private String role;
    private String[] subscriptions;
    private String[] requestIds;
    private String[] answerIds;

}
