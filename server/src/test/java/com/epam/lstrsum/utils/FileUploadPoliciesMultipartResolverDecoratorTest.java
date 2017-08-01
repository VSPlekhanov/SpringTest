package com.epam.lstrsum.utils;

import com.epam.lstrsum.exception.RestrictedMultipartException;
import com.epam.lstrsum.exception.SizeLimitMultipartException;
import org.apache.tomcat.util.http.fileupload.FileUploadBase;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockMultipartHttpServletRequest;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartResolver;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class FileUploadPoliciesMultipartResolverDecoratorTest {

    private FileUploadPoliciesMultipartResolverDecorator resolver;

    private List<String> allowed = Arrays.asList("doc", "pdf");

    @Mock
    private MultipartResolver decorated;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        resolver = new FileUploadPoliciesMultipartResolverDecorator(decorated);
        resolver.setAllowedExtensions(allowed);
    }

    @Test
    public void isRestrictedShouldRestrictInUsualCase() throws Exception {
        assertTrue(resolver.isAllowed("doc"));
    }

    @Test
    public void isRestrictedShouldNotRestrictInUsualCase() throws Exception {
        assertFalse(resolver.isAllowed("exe"));
    }

    @Test(expected = RestrictedMultipartException.class)
    public void resolverShouldThrowRestrictedMultipartExceptionIfFileExtensionIsNotAllowed() {
        MockMultipartFile multipartFile = new MockMultipartFile("file", "file.exe", MediaType.TEXT_HTML.toString(), new byte[0]);
        MockHttpServletRequest request =
                MockMvcRequestBuilders
                        .fileUpload("/attachment")
                        .file(multipartFile)
                        .buildRequest(null);

        when(decorated.resolveMultipart(request)).thenReturn((MultipartHttpServletRequest) request);

        resolver.resolveMultipart(request);
    }

    @Test
    public void resolverShouldResolveValidRequestIfExtensionIsAllowed() {
        MockMultipartFile multipartFile = new MockMultipartFile("file", "file.doc", MediaType.TEXT_HTML.toString(), new byte[0]);
        MockHttpServletRequest request =
                MockMvcRequestBuilders
                        .fileUpload("/attachment")
                        .file(multipartFile)
                        .buildRequest(null);

        when(decorated.resolveMultipart(request)).thenReturn((MultipartHttpServletRequest) request);

        MultipartHttpServletRequest actual = resolver.resolveMultipart(request);

        assertEquals(request, actual);
    }

    @Test(expected = SizeLimitMultipartException.class)
    public void resolverShouldThrowSizeLimitMultipartExceptionWhenDecoratedThrowSizeException() {
        MockMultipartFile multipartFile = new MockMultipartFile("file", "file.doc", MediaType.TEXT_HTML.toString(), new byte[0]);
        MockHttpServletRequest request =
                MockMvcRequestBuilders
                        .fileUpload("/attachment")
                        .file(multipartFile)
                        .buildRequest(null);

        when(decorated.resolveMultipart(request)).thenThrow(validSizeException());

        resolver.resolveMultipart(request);
    }

    private MultipartException validSizeException() {
        return new MultipartException("",
                new IllegalArgumentException(
                        new FileUploadBase.SizeLimitExceededException("to large", 0, 1)));
    }

    public FileUploadPoliciesMultipartResolverDecoratorTest() {
        super();
    }

    @Test
    public void resolverShouldCleanupMultipart() {
        MockMultipartFile multipartFile = new MockMultipartFile("file", "file.doc", MediaType.TEXT_HTML.toString(), new byte[0]);
        MockMultipartFile anotherMultipartFile = new MockMultipartFile("file2", "file2.doc", MediaType.TEXT_HTML.toString(), new byte[0]);

        MockMultipartHttpServletRequest multipartRequest = new MockMultipartHttpServletRequest();
        multipartRequest.setContentType(MediaType.MULTIPART_FORM_DATA_VALUE);
        multipartRequest.setMethod(HttpMethod.POST.name());
        multipartRequest.addFile(multipartFile);
        multipartRequest.addFile(anotherMultipartFile);

        doNothing().when(decorated).cleanupMultipart(any(MultipartHttpServletRequest.class));

        resolver.cleanupMultipart(multipartRequest);

        verify(decorated, times(1)).cleanupMultipart(any(MultipartHttpServletRequest.class));
    }
}