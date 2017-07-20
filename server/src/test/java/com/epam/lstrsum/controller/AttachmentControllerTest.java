package com.epam.lstrsum.controller;

import com.epam.lstrsum.dto.AttachmentAllFieldsDto;
import com.epam.lstrsum.service.AttachmentService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class AttachmentControllerTest {

    @Mock
    private AttachmentService attachmentService;

    private AttachmentController controller;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        controller = new AttachmentController(attachmentService);
    }

    @Test
    public void uploadAttachmentShouldSaveFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile("name", "originalName", "contentType", new byte[5]);
        controller.uploadAttachment(file);

        verify(attachmentService).saveMultipartFile(file);
    }

    @Test
    public void uploadAttachmentShouldReturnValidResponseEntity() throws Exception {
        String savedId = "id";
        MockMultipartFile file = new MockMultipartFile("name", "originalName", "contentType", new byte[5]);
        when(attachmentService.saveMultipartFile(file)).thenReturn(savedId);

        ResponseEntity actual = controller.uploadAttachment(file);

        ResponseEntity expected =
                ResponseEntity
                        .ok()
                        .contentType(MediaType.TEXT_HTML)
                        .header("savedAttachmentId", savedId)
                        .body("redirect:/api/attachment");

        assertEquals(expected, actual);
    }

    @Test
    public void deleteAttachmentShouldDelete() throws Exception {
        String id = "id";
        controller.deleteAttachment(id);

        verify(attachmentService).delete(id);
    }

    @Test
    public void downloadFileShouldReturnValidResponseIfFileIsFoundAndItsMediaTypeIsKnown() throws Exception {
        String id = "id";
        String name = "name";
        MediaType type = MediaType.IMAGE_JPEG;
        AttachmentAllFieldsDto dto = new AttachmentAllFieldsDto(id, name, type.toString(), new byte[10]);

        when(attachmentService.findOne(id)).thenReturn(Optional.of(dto));

        ResponseEntity<Resource> actual = controller.downloadFile(id);

        ResponseEntity expected =
                ResponseEntity
                        .ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + name + "\"")
                        .contentType(type)
                        .body(new ByteArrayResource(dto.getData()));

        assertEquals(expected, actual);
    }

    @Test
    public void downloadFileShouldReturnValidResponseWithAppOcStTypeIfFileIsFoundAndItsMediaTypeIsUnKnown() throws Exception {
        String id = "id";
        String name = "name";
        MediaType type = MediaType.APPLICATION_OCTET_STREAM;
        AttachmentAllFieldsDto dto = new AttachmentAllFieldsDto(id, name, "unknownType", new byte[10]);

        when(attachmentService.findOne(id)).thenReturn(Optional.of(dto));

        ResponseEntity<Resource> actual = controller.downloadFile(id);

        ResponseEntity expected =
                ResponseEntity
                        .ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + name + "\"")
                        .contentType(type)
                        .body(new ByteArrayResource(dto.getData()));

        assertEquals(expected, actual);
    }

    @Test
    public void downloadFileShouldReturnValidResponseIfFileIsNotFound() throws Exception {
        String id = "id";
        when(attachmentService.findOne(id)).thenReturn(Optional.empty());
        ResponseEntity<Resource> actual = controller.downloadFile(id);

        ResponseEntity expected =
                ResponseEntity
                        .notFound()
                        .build();

        assertEquals(expected, actual);
    }

}