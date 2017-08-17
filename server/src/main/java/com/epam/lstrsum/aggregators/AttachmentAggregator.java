package com.epam.lstrsum.aggregators;

import com.epam.lstrsum.converter.AttachmentDtoMapper;
import com.epam.lstrsum.converter.contract.AllFieldModelDtoConverter;
import com.epam.lstrsum.dto.attachment.AttachmentAllFieldsDto;
import com.epam.lstrsum.model.Attachment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AttachmentAggregator implements
        AllFieldModelDtoConverter<Attachment, AttachmentAllFieldsDto> {
    private final AttachmentDtoMapper attachmentMapper;

    @Override
    public AttachmentAllFieldsDto modelToAllFieldsDto(Attachment attachment) {
        return attachmentMapper.modelToAllFieldsDto(attachment);
    }
}
