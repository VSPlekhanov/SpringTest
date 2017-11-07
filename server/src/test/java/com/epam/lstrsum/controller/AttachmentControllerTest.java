package com.epam.lstrsum.controller;

import com.epam.lstrsum.dto.attachment.AttachmentAllFieldsDto;
import com.epam.lstrsum.service.AttachmentService;
import com.epam.lstrsum.service.QuestionService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static com.epam.lstrsum.testutils.InstantiateUtil.someString;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class AttachmentControllerTest {

    @Mock
    private AttachmentService attachmentService;
    @Mock
    private QuestionService questionService;
    @Mock
    private UserRuntimeRequestComponent userRuntimeRequestComponent;

    private AttachmentController controller;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        controller = new AttachmentController(attachmentService, questionService, userRuntimeRequestComponent);
    }

    @Test
    public void downloadFileWithDistributionListUserShouldReturnValidResponseIfFileIsFoundAndItsMediaTypeIsKnown() throws Exception {
        String attachmentId = someString();
        String questionId = someString();
        String name = someString();
        MediaType type = MediaType.IMAGE_JPEG;
        AttachmentAllFieldsDto dto = new AttachmentAllFieldsDto(attachmentId, name, type.toString(), new byte[10]);

        when(attachmentService.findOne(attachmentId)).thenReturn(Optional.of(dto));
        when(userRuntimeRequestComponent.isInDistributionList()).thenReturn(true);

        ResponseEntity<Resource> actual = controller.downloadFile(attachmentId, questionId);

        ResponseEntity expected =
                ResponseEntity
                        .ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + name + "\"")
                        .contentType(type)
                        .body(new ByteArrayResource(dto.getData()));

        assertEquals(expected, actual);
    }

    @Test
    public void downloadFileAllowedSubUserShouldReturnValidResponseIfFileIsFoundAndItsMediaTypeIsKnown() throws Exception {
        String attachmentId = someString();
        String questionId = someString();
        String name = someString();
        MediaType type = MediaType.IMAGE_JPEG;
        AttachmentAllFieldsDto dto = new AttachmentAllFieldsDto(attachmentId, name, type.toString(), new byte[10]);

        when(userRuntimeRequestComponent.isInDistributionList()).thenReturn(false);
        when(userRuntimeRequestComponent.getEmail()).thenReturn(someString());
        when(attachmentService.findOneAllowedSub(anyString(), any(), anyString())).thenReturn(Optional.of(dto));

        ResponseEntity<Resource> actual = controller.downloadFile(attachmentId, questionId);

        ResponseEntity expected =
                ResponseEntity
                        .ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + name + "\"")
                        .contentType(type)
                        .body(new ByteArrayResource(dto.getData()));

        assertEquals(expected, actual);
    }

    @Test
    public void downloadFileWithDistributionListUserShouldReturnValidResponseWithAppOcStTypeIfFileIsFoundAndItsMediaTypeIsUnKnown() throws
            Exception {
        String attachmentId = someString();
        String questionId = someString();
        String name = someString();
        MediaType type = MediaType.APPLICATION_OCTET_STREAM;
        AttachmentAllFieldsDto dto = new AttachmentAllFieldsDto(attachmentId, name, "unknownType", new byte[10]);

        when(attachmentService.findOne(attachmentId)).thenReturn(Optional.of(dto));
        when(userRuntimeRequestComponent.isInDistributionList()).thenReturn(true);

        ResponseEntity<Resource> actual = controller.downloadFile(attachmentId, questionId);

        ResponseEntity expected =
                ResponseEntity
                        .ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + name + "\"")
                        .contentType(type)
                        .body(new ByteArrayResource(dto.getData()));

        assertEquals(expected, actual);
    }

    @Test
    public void downloadFileWithDistributionListUserShouldReturnValidResponseIfFileIsNotFound() throws Exception {
        String attachmentId = someString();
        String questionId = someString();
        when(attachmentService.findOne(attachmentId)).thenReturn(Optional.empty());
        when(userRuntimeRequestComponent.isInDistributionList()).thenReturn(true);
        ResponseEntity<Resource> actual = controller.downloadFile(attachmentId, questionId);

        ResponseEntity expected =
                ResponseEntity
                        .notFound()
                        .build();

        assertEquals(expected, actual);
    }
}