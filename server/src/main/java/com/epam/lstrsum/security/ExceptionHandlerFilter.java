package com.epam.lstrsum.security;

import com.epam.lstrsum.exception.NoSuchUserException;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@NoArgsConstructor
public class ExceptionHandlerFilter extends OncePerRequestFilter {

    /**
     * A method to set http response status code to 404 (NOT_FOUND) instead of default code 500 (INTERNAL_SERVER_ERROR)
     * in case of failed user authentication after success epam sso authentication. See {@link CustomResourceServerTokenServices}
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws
            ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (NoSuchUserException e) {
            setErrorResponse(HttpStatus.NOT_FOUND, response, e);
        }
    }

    private void setErrorResponse(HttpStatus status, HttpServletResponse response, Throwable ex) {
        response.setStatus(status.value());
    }
}