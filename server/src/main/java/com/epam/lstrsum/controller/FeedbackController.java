package com.epam.lstrsum.controller;

import com.epam.lstrsum.dto.feedback.FeedbackPostDto;
import com.epam.lstrsum.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    final private FeedbackService feedbackService;

    @PostMapping
    public ResponseEntity<String> sendFeedback(@RequestPart("dtoObject") FeedbackPostDto dtoObject,
                                               @RequestPart(value = "files", required = false) MultipartFile[] files)
            throws IOException {
        feedbackService.sendFeedback(dtoObject, files);
        return ResponseEntity.ok("Feedback was sent");
    }
}
