package com.epam.lstrsum.converter;

import com.epam.lstrsum.dto.feedback.FeedbackPostDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

import static java.util.Objects.nonNull;

public class FeedbackPostDtoHttpMessageConverter implements HttpMessageConverter<FeedbackPostDto> {

    @Override
    public boolean canRead(Class<?> clazz, MediaType mediaType) {
        return FeedbackPostDto.class == clazz;
    }

    @Override
    public boolean canWrite(Class<?> clazz, MediaType mediaType) {
        return FeedbackPostDto.class == clazz;
    }

    @Override
    public List<MediaType> getSupportedMediaTypes() {
        return Arrays.asList(new MediaType[]{MediaType.MULTIPART_FORM_DATA});
    }

    @Override
    public FeedbackPostDto read(Class<? extends FeedbackPostDto> clazz, HttpInputMessage inputMessage)
            throws IOException, HttpMessageNotReadableException {
        InputStream istream = inputMessage.getBody();
        Charset charset = getContentTypeCharset(inputMessage.getHeaders().getContentType());
        String requestString = IOUtils.toString(istream, charset.name());

        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(requestString, FeedbackPostDto.class);
    }

    @Override
    public void write(FeedbackPostDto t, MediaType contentType, HttpOutputMessage outputMessage)
            throws IOException, HttpMessageNotWritableException {
    }

    private Charset getContentTypeCharset(MediaType contentType) {
        if (nonNull(contentType) && nonNull(contentType.getCharset())) {
            return contentType.getCharset();
        } else {
            return Charset.forName("UTF-8");
        }
    }

}
