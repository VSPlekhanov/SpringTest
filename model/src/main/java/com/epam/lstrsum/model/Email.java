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
@Document(collection = Email.EMAIL_COLLECTION_NAME)
public class Email {
    public final static String EMAIL_COLLECTION_NAME = "Email";

    @Id
    private String id;
    private String from;
    private String subject;
    private String fileName;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Email email = (Email) o;

        if (id != null ? !id.equals(email.id) : email.id != null) return false;
        if (from != null ? !from.equals(email.from) : email.from != null) return false;
        if (subject != null ? !subject.equals(email.subject) : email.subject != null) return false;
        return fileName != null ? fileName.equals(email.fileName) : email.fileName == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (from != null ? from.hashCode() : 0);
        result = 31 * result + (subject != null ? subject.hashCode() : 0);
        result = 31 * result + (fileName != null ? fileName.hashCode() : 0);
        return result;
    }
}
