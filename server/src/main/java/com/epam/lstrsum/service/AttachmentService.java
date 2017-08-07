package com.epam.lstrsum.service;

import com.epam.lstrsum.aggregators.AttachmentAggregator;
import com.epam.lstrsum.dto.attachment.AttachmentAllFieldsDto;
import com.epam.lstrsum.model.Attachment;
import com.epam.lstrsum.persistence.AttachmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final AttachmentAggregator attachmentAggregator;

    public AttachmentAllFieldsDto save(AttachmentAllFieldsDto attachmentAllFieldsDto) {
        Attachment attachment = Attachment.builder()
                .data(attachmentAllFieldsDto.getData())
                .name(attachmentAllFieldsDto.getName())
                .type(attachmentAllFieldsDto.getType())
                .build();

        Attachment save = attachmentRepository.save(attachment);
        log.debug("Add new Attachment with id {}", save.getId());

        return attachmentAggregator.modelToAllFieldsDto(save);
    }

    public String saveMultipartFile(MultipartFile file) throws IOException {
        Attachment attachment = Attachment.builder()
                .data(file.getBytes())
                .name(file.getOriginalFilename())
                .type(file.getContentType())
                .build();

        return attachmentRepository.save(attachment).getId();
    }

    public Optional<AttachmentAllFieldsDto> findOne(String id) {
        return Optional
                .ofNullable(attachmentRepository.findOne(id))
                .map(attachmentAggregator::modelToAllFieldsDto);
    }

    public void delete(String id) {
        log.debug("Delete attachment with id {}", id);
        attachmentRepository.delete(id);
    }
}
