package com.epam.lstrsum.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = Attachment.ATTACHMENT_COLLECTION_NAME)
public class Attachment {
    public final static String ATTACHMENT_COLLECTION_NAME = "attachment";

    @Id
    private String id;
    private String name;
    private String type;
    private byte[] data;

}
