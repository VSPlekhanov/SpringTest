package com.epam.lstrsum.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Builder
@Document(collection = Attachment.ATTACHMENT_COLLECTION_NAME)
public class Attachment {
    public final static String ATTACHMENT_COLLECTION_NAME = "Attachment";

    @Id
    private String id;
    private String name;
    private String type;
    private byte[] data;


}
