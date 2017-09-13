package com.epam.lstrsum.configuration;

import com.epam.lstrsum.enums.UserRoleType;
import com.epam.lstrsum.security.CustomResourceServerTokenServices;
import com.epam.lstrsum.security.cert.TrustAllCertificatesSSL;
import com.epam.lstrsum.security.role.ResourceBundleRoleService;
import com.epam.lstrsum.security.role.RoleService;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.TestingAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.filter.OAuth2ClientAuthenticationProcessingFilter;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails;
import org.springframework.security.oauth2.common.AuthenticationScheme;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;
import org.springframework.security.oauth2.provider.error.OAuth2AuthenticationEntryPoint;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
    @Setter
    private List<String> envsToDisableCsrf;
    @Autowired
    private Environment env;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        RoleService roleService = roleService();

        if (firstContainsAny(Arrays.asList(env.getActiveProfiles()), envsToDisableCsrf)) {
            http.csrf().disable();
        }

        http.authorizeRequests()
                .antMatchers("/sso/**").permitAll().and();

        Map<String, String[]> rolesRequestsMapping = roleService.getRolesRequestsMapping();

        for (Map.Entry<String, String[]> entry : rolesRequestsMapping.entrySet()) {
            http.authorizeRequests().antMatchers(entry.getKey()).hasAnyAuthority(entry.getValue()).and();
        }

        http
                .addFilterBefore(auth2ClientAuthenticationProcessingFilter(), BasicAuthenticationFilter.class)
                .exceptionHandling()
                .authenticationEntryPoint(authenticationEntryPoint())
                .accessDeniedPage("/unauthorized");
    }

    private boolean firstContainsAny(List<String> envs, List<String> envsToDisable) {
        return !Collections.disjoint(envs, envsToDisable);
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(authenticationProvider());
    }


    @Bean
    public OAuth2ClientAuthenticationProcessingFilter auth2ClientAuthenticationProcessingFilter() {
        OAuth2ClientAuthenticationProcessingFilter af =
                new OAuth2ClientAuthenticationProcessingFilter("/sso/login");
        af.setTokenServices(tokenService());
        af.restTemplate = new OAuth2RestTemplate(authorizationCodeResourceDetails(), oAuth2ClientContext);
        af.setAuthenticationSuccessHandler(new SavedRequestAwareAuthenticationSuccessHandler());
        af.setRememberMeServices(rememberMeServices());
        return af;
    }

    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return new ProviderManager(Collections.singletonList(authenticationProvider()));
    }

    @Bean
    public TestingAuthenticationProvider authenticationProvider() {
        return new TestingAuthenticationProvider();
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return new OAuth2AuthenticationEntryPoint();
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
        return new CustomResourceServerTokenServices(roleService(), authorizationCodeResourceDetails());
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
                final boolean isAdmin = auth.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals(UserRoleType.ADMIN.name()));
                response.addCookie(new Cookie("role", (isAdmin) ? UserRoleType.ADMIN.name() : UserRoleType.EXTENDED_USER.name()));
            }
        };
    }
}
