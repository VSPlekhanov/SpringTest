package com.epam.lstrsum.service;

import com.epam.lstrsum.dto.attachment.AttachmentAllFieldsDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

public interface AttachmentService {
    AttachmentAllFieldsDto save(AttachmentAllFieldsDto attachmentAllFieldsDto);

    String saveMultipartFile(MultipartFile file) throws IOException;

    Optional<AttachmentAllFieldsDto> findOne(String id);

    void delete(String id);
}
