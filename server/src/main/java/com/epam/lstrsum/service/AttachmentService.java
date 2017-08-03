package com.epam.lstrsum.service;

import com.epam.lstrsum.converter.AttachmentDtoConverter;
import com.epam.lstrsum.dto.attachment.AttachmentAllFieldsDto;
import com.epam.lstrsum.model.Attachment;
import lombok.RequiredArgsConstructor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AttachmentService {

    private final CrudRepository<Attachment, String> repository;
    private final AttachmentDtoConverter converter;

    public AttachmentAllFieldsDto save(AttachmentAllFieldsDto attachmentAllFieldsDto) {
        Attachment attachment = Attachment.builder()
                .data(attachmentAllFieldsDto.getData())
                .name(attachmentAllFieldsDto.getFileName())
                .type(attachmentAllFieldsDto.getFileType())
                .build();

        return converter.modelToAllFieldsDto(repository.save(attachment));
    }

    public String saveMultipartFile(MultipartFile file) throws IOException {
        Attachment attachment = Attachment.builder()
                .data(file.getBytes())
                .name(file.getOriginalFilename())
                .type(file.getContentType())
                .build();

        return repository.save(attachment).getId();
    }

    public Optional<AttachmentAllFieldsDto> findOne(String id) {
        return Optional
                .ofNullable(repository.findOne(id))
                .map(converter::modelToAllFieldsDto);
    }

    public void delete(String id) {
        repository.delete(id);
    }

}
