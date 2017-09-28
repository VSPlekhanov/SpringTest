package com.epam.lstrsum.controller;

import com.epam.lstrsum.dto.attachment.AttachmentAllFieldsDto;
import com.epam.lstrsum.service.AttachmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/attachment")
@RequiredArgsConstructor
public class AttachmentController {
    private final AttachmentService attachmentService;

    @GetMapping("/{id}")
    public ResponseEntity<Resource> downloadFile(@PathVariable("id") String id) {
        Optional<AttachmentAllFieldsDto> one = attachmentService.findOne(id);

        return one
                .map(this::buildResponse)
                .orElse(notFoundResponse());
    }

    private ResponseEntity<Resource> notFoundResponse() {
        return ResponseEntity
                .notFound()
                .build();
    }

    private ResponseEntity<Resource> buildResponse(AttachmentAllFieldsDto dto) {
        Resource file = new ByteArrayResource(dto.getData());

        MediaType mediaType;

        try {
            mediaType = MediaType.valueOf(dto.getType());
        } catch (InvalidMediaTypeException e) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }

        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + dto.getName() + "\"")
                .contentType(mediaType)
                .body(file);
    }
}

