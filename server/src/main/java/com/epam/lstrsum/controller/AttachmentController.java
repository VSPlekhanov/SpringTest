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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@RestController
@RequestMapping("/api/attachment")
@RequiredArgsConstructor
public class AttachmentController {

    private final AttachmentService attachmentService;

    @PostMapping
    public ResponseEntity<String> uploadAttachment(@RequestParam("file") MultipartFile file) throws IOException {
        String savedId = attachmentService.saveMultipartFile(file);

        return ResponseEntity
                .ok()
                .contentType(MediaType.TEXT_HTML)
                .header("savedAttachmentId", savedId)
                .body("redirect:/api/attachment");
    }

    @DeleteMapping("/{id}")
    public void deleteAttachment(@PathVariable("id") String id) {
        attachmentService.delete(id);
    }

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
            mediaType = MediaType.valueOf(dto.getFileType());
        } catch (InvalidMediaTypeException e) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }

        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + dto.getFileName() + "\"")
                .contentType(mediaType)
                .body(file);
    }
}

