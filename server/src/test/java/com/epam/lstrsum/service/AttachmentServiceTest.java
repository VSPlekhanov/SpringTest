package com.epam.lstrsum.service;

import com.epam.lstrsum.converter.ModelDtoConverter;
import com.epam.lstrsum.dto.AttachmentAllFieldsDto;
import com.epam.lstrsum.model.Attachment;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.data.repository.CrudRepository;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class AttachmentServiceTest {

    @Mock
    private CrudRepository<Attachment, String> repository;

    private ModelDtoConverter converter = new ModelDtoConverter();

    private AttachmentService attachmentService;

    @Before
    public void setUp() {
        initMocks(this);
        attachmentService = new AttachmentService(repository, converter);
    }

    @Test
    public void newAttachmentShouldBeSaved() throws Exception {
        AttachmentAllFieldsDto newAttachment = new AttachmentAllFieldsDto(null, "testFile", "image/jpeg", new byte[]{1, 2, 3});
        Attachment attachment = new Attachment(null, newAttachment.getFileName(), newAttachment.getFileType(), newAttachment.getData());

        when(repository.save(attachment)).thenReturn(attachment);
        attachmentService.save(newAttachment);

        verify(repository).save(attachment);
    }

    @Test
    public void newMultipartFileShouldBeSaved() throws Exception {
        String originalFileName = "originalFileName";
        String contentType = "contentType";
        byte[] content = {1, 2, 3};

        MockMultipartFile file = new MockMultipartFile("fileName", originalFileName, contentType, content);
        Attachment expected = new Attachment(null, originalFileName, contentType, content);

        when(repository.save(expected)).thenReturn(expected);
        attachmentService.saveMultipartFile(file);
        verify(repository).save(expected);
    }

    @Test
    public void findOneShouldFindExistingObject() throws Exception {
        String existingId = "someId";
        Attachment att = new Attachment(existingId, "name", "type", new byte[]{1, 2, 3});
        when(repository.findOne(existingId)).thenReturn(att);

        Optional<AttachmentAllFieldsDto> one = attachmentService.findOne(existingId);

        AttachmentAllFieldsDto expected = converter.attachmentToAllFieldDto(att);
        AttachmentAllFieldsDto actual = one.orElseThrow(() -> new AssertionError("Expected not found!"));

        assertEquals(expected, actual);
    }

    @Test
    public void fineOneShouldReturnEmptyOptionalIfNotFound() throws Exception {
        String notExistingId = "someId";
        when(repository.findOne(notExistingId)).thenReturn(null);

        Optional<AttachmentAllFieldsDto> one = attachmentService.findOne(notExistingId);

        assertEquals(Optional.empty(), one);
    }

    @Test
    public void deleteShouldRemoveAttachment() throws Exception {
        String id = "id";

        attachmentService.delete(id);
        verify(repository).delete(id);
    }

}