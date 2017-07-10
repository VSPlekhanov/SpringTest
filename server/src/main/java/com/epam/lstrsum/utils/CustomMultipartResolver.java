package com.epam.lstrsum.utils;

import com.epam.lstrsum.exceptions.RestrictedMultipartException;
import com.epam.lstrsum.exceptions.SizeLimitMultipartException;
import lombok.Setter;
import org.apache.tomcat.util.http.fileupload.FileUploadBase;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

/**
 * This class is able to check if uploaded file type is not in restriction list.
 * If so the RestrictedMultipartException will be thrown.
 * <p>
 * Also it can catch any MultipartExceptions and translate them to our custom exceptions.
 * Such as SizeLimitMultipartException.
 */
@Component
@ConfigurationProperties(prefix = "multipart")
public class CustomMultipartResolver extends StandardServletMultipartResolver {

    private static final String DEFAULT_RESTRICTIONS = "exe,bat,com,sh";

    @Setter
    private String restrictions = DEFAULT_RESTRICTIONS;

    @Override
    public MultipartHttpServletRequest resolveMultipart(HttpServletRequest request) throws MultipartException {
        try {
            MultipartHttpServletRequest multipartHttpServletRequest = super.resolveMultipart(request);

            for (MultipartFile file : multipartHttpServletRequest.getFileMap().values()) {
                String filename = file.getOriginalFilename();
                String extension = filename.substring(filename.lastIndexOf(".") + 1).trim();
                if (isRestricted(extension))
                    throw new RestrictedMultipartException("File type " + extension + " is restricted!");
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

    // If you know better solution, please, refactor.
    private boolean isSizeLimitException(MultipartException e) {
        return Optional.ofNullable(e.getCause())
                .map((th) -> Optional.ofNullable(th.getCause()))
                .flatMap(o -> o)
                .map(th -> th.getClass().equals(FileUploadBase.SizeLimitExceededException.class))
                .orElse(false);
    }

    public boolean isRestricted(String fileExtension) {
        String regexp = String.format("(.*(,|^|[ ].*|(\\W&\\D))%s(,|$|[ ].*|(\\W&\\D)).*)", fileExtension);
        return restrictions.matches(regexp);
    }
}
