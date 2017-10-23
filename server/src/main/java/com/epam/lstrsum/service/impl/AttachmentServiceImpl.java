package com.epam.lstrsum.service.impl;

import com.epam.lstrsum.aggregators.AttachmentAggregator;
import com.epam.lstrsum.dto.attachment.AttachmentAllFieldsDto;
import com.epam.lstrsum.model.Attachment;
import com.epam.lstrsum.model.Question;
import com.epam.lstrsum.model.User;
import com.epam.lstrsum.persistence.AttachmentRepository;
import com.epam.lstrsum.service.AttachmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

import static java.util.Objects.isNull;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttachmentServiceImpl implements AttachmentService {
    private final AttachmentRepository attachmentRepository;
    private final AttachmentAggregator attachmentAggregator;

    @Override
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

    @Override
    public String saveMultipartFile(MultipartFile file) throws IOException {
        Attachment attachment = Attachment.builder()
                .data(file.getBytes())
                .name(file.getOriginalFilename())
                .type(file.getContentType())
                .build();

        String attachmentId = attachmentRepository.save(attachment).getId();
        log.debug("Save attachment with id {}", attachmentId);
        return attachmentId;
    }

    @Override
    public Optional<AttachmentAllFieldsDto> findOne(String id) {
        return Optional
                .ofNullable(attachmentRepository.findOne(id))
                .map(attachmentAggregator::modelToAllFieldsDto);
    }

    @Override
    public Optional<AttachmentAllFieldsDto> findOneAllowedSub(String id, Question question, String userEmail) {
        return questionExistAndUserAllowedSubOnIt(question, userEmail) ? findOne(id) : Optional.empty();
    }

    private boolean questionExistAndUserAllowedSubOnIt(Question question, String userEmail) {
        return !isNull(question) && question.getAllowedSubs().stream().map(User::getEmail).filter(e -> e.equals(userEmail)).count() == 1;
    }

    @Override
    public void delete(String id) {
        log.debug("Delete attachment with id {}", id);
        attachmentRepository.delete(id);
    }
}
