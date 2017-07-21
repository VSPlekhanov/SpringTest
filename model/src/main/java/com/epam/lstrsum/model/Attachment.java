package com.epam.lstrsum.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

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
