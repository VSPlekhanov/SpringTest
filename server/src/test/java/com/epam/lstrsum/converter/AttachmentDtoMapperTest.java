package com.epam.lstrsum.converter;

import com.epam.lstrsum.SetUpDataBaseCollections;
import com.epam.lstrsum.model.Attachment;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static com.epam.lstrsum.testutils.InstantiateUtil.someAttachment;
import static org.assertj.core.api.Assertions.assertThat;

public class AttachmentDtoMapperTest extends SetUpDataBaseCollections {
    @Autowired
    private AttachmentDtoMapper attachmentMapper;

    @Test
    public void modelToAllFieldsDto() throws Exception {
        Attachment attachment = someAttachment();

        assertThat(attachmentMapper.modelToAllFieldsDto(attachment))
                .satisfies(a -> {
                    assertThat(a.getData()).isEqualTo(attachment.getData());
                    assertThat(a.getName()).isEqualTo(attachment.getName());
                    assertThat(a.getType()).isEqualTo(attachment.getType());
                    assertThat(a.getId()).isEqualTo(attachment.getId());
                });
    }
}
