package com.epam.lstrsum.utils;

import com.epam.lstrsum.exception.RestrictedMultipartException;
import com.epam.lstrsum.exception.SizeLimitMultipartException;
import lombok.Setter;
import org.apache.tomcat.util.http.fileupload.FileUploadBase;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * This class is decorator for general MultipartResolver.
 * <p>
 * Its responsibility is to check if uploaded file type is in allowed list.
 * If not the RestrictedMultipartException will be thrown.
 * <p>
 * Also it can catch any MultipartExceptions and translate them to our custom exception.
 * Such as SizeLimitMultipartException.
 */
@Component
@ConfigurationProperties(prefix = "multipart")
public class FileUploadPoliciesMultipartResolverDecorator implements MultipartResolver {

    private MultipartResolver resolver = defaultResolver();

    @Setter
    private List<String> allowedExtensions = Collections.emptyList();

    public FileUploadPoliciesMultipartResolverDecorator() {
    }

    public FileUploadPoliciesMultipartResolverDecorator(MultipartResolver resolver) {
        this.resolver = resolver;
    }


    @Override
    public MultipartHttpServletRequest resolveMultipart(HttpServletRequest request) throws MultipartException {
        try {
            MultipartHttpServletRequest multipartHttpServletRequest = resolver.resolveMultipart(request);

            for (MultipartFile file : multipartHttpServletRequest.getFileMap().values()) {
                String filename = file.getOriginalFilename();
                String extension = filename.substring(filename.lastIndexOf(".") + 1).trim();
                if (!isAllowed(extension))
                    throw new RestrictedMultipartException("File type " + extension + " is not allowed!");
            }

            return multipartHttpServletRequest;
        } catch (MultipartException e) {
            if (isSizeLimitException(e)) {
                throw new SizeLimitMultipartException("File size is to large", e);
            } else {
                throw e;
            }
        }
    }

    @Override
    public boolean isMultipart(HttpServletRequest request) {
        return resolver.isMultipart(request);
    }

    @Override
    public void cleanupMultipart(MultipartHttpServletRequest request) {
        resolver.cleanupMultipart(request);
    }

    // If you know better solution, please, refactor.
    private boolean isSizeLimitException(MultipartException e) {
        return Optional.ofNullable(e.getCause())
                .map((th) -> Optional.ofNullable(th.getCause()))
                .flatMap(o -> o)
                .map(th -> th.getClass().equals(FileUploadBase.SizeLimitExceededException.class))
                .orElse(false);
    }

    public boolean isAllowed(String fileExtension) {
        return allowedExtensions.stream()
                .anyMatch(fileExtension::equals);
    }

    private StandardServletMultipartResolver defaultResolver() {
        return new StandardServletMultipartResolver();
    }
}
