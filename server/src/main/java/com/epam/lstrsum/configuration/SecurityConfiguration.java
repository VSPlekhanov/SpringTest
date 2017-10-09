package com.epam.lstrsum.configuration;

import com.epam.lstrsum.enums.UserRoleType;
import com.epam.lstrsum.security.CustomResourceServerTokenServices;
import com.epam.lstrsum.security.ExceptionHandlerFilter;
import com.epam.lstrsum.security.cert.TrustAllCertificatesSSL;
import com.epam.lstrsum.security.role.ResourceBundleRoleService;
import com.epam.lstrsum.security.role.RoleService;
import com.epam.lstrsum.service.UserService;
import lombok.Setter;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.filter.OAuth2ClientAuthenticationProcessingFilter;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails;
import org.springframework.security.oauth2.common.AuthenticationScheme;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.ResourceBundle;

@Configuration
@EnableOAuth2Client
@ConfigurationProperties(prefix = "security")
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    static {
        new TrustAllCertificatesSSL();
    }

    @Autowired
    @SuppressWarnings("all")
    OAuth2ClientContext oAuth2ClientContext;
    @Setter
    private String authorizationId;
    @Setter
    private String authorizationClientId;
    @Setter
    private String authorizationAccessTokenUri;
    @Setter
    private String authorizationUserUri;

    @Autowired
    private UserService userService;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        RoleService roleService = roleService();

        http
                .authorizeRequests()
                .antMatchers("/sso/login")
                .permitAll().and();

        Map<String, String[]> rolesRequestsMapping = roleService.getRolesRequestsMapping();

        for (Map.Entry<String, String[]> entry : rolesRequestsMapping.entrySet()) {
            http.authorizeRequests().antMatchers(entry.getKey()).hasAnyAuthority(entry.getValue()).and();
        }

        http.csrf()
                .csrfTokenRepository(csrfTokenRepository()).and();
        http
                .addFilterAfter(csrfHeaderFilter(), CsrfFilter.class)
                .addFilterBefore(oauthFilter(), BasicAuthenticationFilter.class)
                .addFilterAfter(exceptionHandlerFilter(), SecurityContextPersistenceFilter.class)
                .exceptionHandling()
                .authenticationEntryPoint(authenticationEntryPoint());


    }

    private Filter exceptionHandlerFilter() {
        return new ExceptionHandlerFilter();
    }

    private OAuth2ClientAuthenticationProcessingFilter oauthFilter() {
        OAuth2ClientAuthenticationProcessingFilter filter =
                new OAuth2ClientAuthenticationProcessingFilter("/sso/login");
        filter.setTokenServices(tokenService());
        filter.restTemplate = new OAuth2RestTemplate(authorizationCodeResourceDetails(), oAuth2ClientContext);
        filter.setAuthenticationSuccessHandler(new SavedRequestAwareAuthenticationSuccessHandler());
        filter.setRememberMeServices(rememberMeServices());
        return filter;
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return new LoginUrlAuthenticationEntryPoint("/sso/login");
    }

    @Bean
    public AuthorizationCodeResourceDetails authorizationCodeResourceDetails() {
        AuthorizationCodeResourceDetails details = new AuthorizationCodeResourceDetails();
        details.setId(authorizationId);
        details.setClientId(authorizationClientId);
        details.setAccessTokenUri(authorizationAccessTokenUri);
        details.setUserAuthorizationUri(authorizationUserUri);
        details.setClientAuthenticationScheme(AuthenticationScheme.form);
        details.setAuthenticationScheme(AuthenticationScheme.query);
        return details;
    }

    @Bean
    public ResourceServerTokenServices tokenService() {
        return new CustomResourceServerTokenServices(roleService(), authorizationCodeResourceDetails(), userService);
    }

    @Bean
    public RoleService roleService() {
        return new ResourceBundleRoleService(ResourceBundle.getBundle("security-roles"));
    }

    @Bean
    public RememberMeServices rememberMeServices() {
        return new RememberMeServices() {
            @Override
            public Authentication autoLogin(HttpServletRequest request, HttpServletResponse response) {
                return null;
            }

            @Override
            public void loginFail(HttpServletRequest request, HttpServletResponse response) {
            }

            @Override
            public void loginSuccess(HttpServletRequest request, HttpServletResponse response, Authentication auth) {
                val isAdmin = auth.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals(UserRoleType.ROLE_ADMIN.name()));
                response.addCookie(new Cookie("role", (isAdmin) ? UserRoleType.ROLE_ADMIN.name() : UserRoleType.ROLE_EXTENDED_USER.name()));
            }
        };
    }

    private Filter csrfHeaderFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                    throws ServletException, IOException {
                CsrfToken csrf = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
                if (csrf != null) {
                    Cookie cookie = WebUtils.getCookie(request, "XSRF-TOKEN");
                    String token = csrf.getToken();
                    if (cookie == null || token != null && !token.equals(cookie.getValue())) {
                        cookie = new Cookie("XSRF-TOKEN", token);
                        cookie.setPath("/");
                        response.addCookie(cookie);
                    }
                }
                filterChain.doFilter(request, response);
            }
        };
    }

    private CsrfTokenRepository csrfTokenRepository() {
        HttpSessionCsrfTokenRepository repository = new HttpSessionCsrfTokenRepository();
        repository.setHeaderName("X-XSRF-TOKEN");
        return repository;
    }
}
