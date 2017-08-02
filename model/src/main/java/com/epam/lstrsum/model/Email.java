package com.epam.lstrsum.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Document(collection = Email.EMAIL_COLLECTION_NAME)
public class Email {
    public final static String EMAIL_COLLECTION_NAME = "email";

    @Id
    private String id;
    private String from;
    private String subject;
    private String fileName;
}
