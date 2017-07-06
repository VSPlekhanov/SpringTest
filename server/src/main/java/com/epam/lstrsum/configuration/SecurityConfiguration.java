package com.epam.lstrsum.configuration;

import com.epam.lstrsum.security.CustomResourceServerTokenServices;
import com.epam.lstrsum.security.cert.TrustAllCertificatesSSL;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.TestingAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.filter.OAuth2ClientAuthenticationProcessingFilter;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails;
import org.springframework.security.oauth2.common.AuthenticationScheme;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import java.util.Collections;

@Configuration
@EnableOAuth2Client
@ConfigurationProperties(prefix = "security")
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Setter
    private String authorizationId;
    @Setter
    private String authorizationClientId;
    @Setter
    private String authorizationAccessTokenUri;
    @Setter
    private String authorizationUserUri;

    @Autowired
    @SuppressWarnings("all")
    OAuth2ClientContext oAuth2ClientContext;

    static {
        new TrustAllCertificatesSSL();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .antMatchers("/sso/**").permitAll()
                .antMatchers("/**").fullyAuthenticated()
                .and()
                .addFilterBefore(auth2ClientAuthenticationProcessingFilter(), BasicAuthenticationFilter.class)
                .exceptionHandling().authenticationEntryPoint(authenticationEntryPoint());
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
        return new CustomResourceServerTokenServices();
    }


}
