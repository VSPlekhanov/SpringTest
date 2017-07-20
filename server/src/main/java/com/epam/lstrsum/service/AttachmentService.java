package com.epam.lstrsum.service;

import com.epam.lstrsum.converter.ModelDtoConverter;
import com.epam.lstrsum.dto.AttachmentAllFieldsDto;
import com.epam.lstrsum.model.Attachment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@Service
public class AttachmentService {

    private final CrudRepository<Attachment, String> repository;
    private final ModelDtoConverter converter;

    @Autowired
    public AttachmentService(CrudRepository<Attachment, String> repository, ModelDtoConverter converter) {
        this.repository = repository;
        this.converter = converter;
    }


    public AttachmentAllFieldsDto save(AttachmentAllFieldsDto attachmentAllFieldsDto) {
        Attachment attachment = new Attachment();

        attachment.setData(attachmentAllFieldsDto.getData());
        attachment.setName(attachmentAllFieldsDto.getFileName());
        attachment.setType(attachmentAllFieldsDto.getFileType());

        return converter.attachmentToAllFieldDto(repository.save(attachment));
    }

    public String saveMultipartFile(MultipartFile file) throws IOException {
        Attachment attachment = new Attachment();

        attachment.setData(file.getBytes());
        attachment.setName(file.getOriginalFilename());
        attachment.setType(file.getContentType());

        return repository.save(attachment).getId();
    }

    public Optional<AttachmentAllFieldsDto> findOne(String id) {
        return Optional
                .ofNullable(repository.findOne(id))
                .map(converter::attachmentToAllFieldDto);
    }

    public void delete(String id) {
        repository.delete(id);
    }

}
