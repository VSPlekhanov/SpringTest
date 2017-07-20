package com.epam.lstrsum.persistence;

import com.epam.lstrsum.model.Attachment;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AttachmentsRepository extends MongoRepository<Attachment, String> {
}
