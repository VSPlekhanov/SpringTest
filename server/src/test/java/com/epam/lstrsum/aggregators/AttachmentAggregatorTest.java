package com.epam.lstrsum.aggregators;

import com.epam.lstrsum.converter.AttachmentDtoMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static com.epam.lstrsum.InstantiateUtil.someAttachment;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

public class AttachmentAggregatorTest {
    private AttachmentAggregator aggregator;

    @Mock
    private AttachmentDtoMapper attachmentMapper;

    @Before
    public void setUp() {
        initMocks(this);
        aggregator = new AttachmentAggregator(attachmentMapper);
    }

    @Test
    public void modelToAllFieldsDto() throws Exception {
        aggregator.modelToAllFieldsDto(someAttachment());

        verify(attachmentMapper, times(1)).modelToAllFieldsDto(any());
    }
}
