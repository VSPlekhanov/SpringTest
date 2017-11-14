package com.epam.lstrsum.controller;

import com.epam.lstrsum.dto.attachment.AttachmentAllFieldsDto;
import com.epam.lstrsum.model.Question;
import com.epam.lstrsum.service.AttachmentService;
import com.epam.lstrsum.service.QuestionService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.mail.internet.MimeUtility;
import java.util.Optional;

@RestController
@RequestMapping("/api/attachment")
@RequiredArgsConstructor
public class AttachmentController {
    private final AttachmentService attachmentService;
    private final QuestionService questionService;
    private final UserRuntimeRequestComponent userRuntimeRequestComponent;

    @GetMapping("/{id}")
    public ResponseEntity<Resource> downloadFile(@PathVariable("id") String attachmentId, @RequestParam String questionId) {
        String userEmail = userRuntimeRequestComponent.getEmail();

        Optional<AttachmentAllFieldsDto> one;
        if (currentUserInDistributionList()) {
            one = attachmentService.findOne(attachmentId);
        } else {
            Question question = questionService.getQuestionById(questionId);
            one = attachmentService.findOneAllowedSub(attachmentId, question, userEmail);
        }

        return one
                .map(this::buildResponse)
                .orElse(notFoundResponse());
    }

    private ResponseEntity<Resource> notFoundResponse() {
        return ResponseEntity
                .notFound()
                .build();
    }

    @SneakyThrows
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
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\""
                        + MimeUtility.encodeWord(dto.getName(), "utf-8", "Q") + "\"")
                .contentType(mediaType)
                .body(file);
    }

    private boolean currentUserInDistributionList() {
        return userRuntimeRequestComponent.isInDistributionList();
    }
}
