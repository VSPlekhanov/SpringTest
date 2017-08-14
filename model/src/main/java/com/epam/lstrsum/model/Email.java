package com.epam.lstrsum.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Builder
@Document(collection = Email.EMAIL_COLLECTION_NAME)
public class Email {
    public final static String EMAIL_COLLECTION_NAME = "Email";

    @Id
    private String id;
    private String from;
    private String subject;
    private String fileName;
}
