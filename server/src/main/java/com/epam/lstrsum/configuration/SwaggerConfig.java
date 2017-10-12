package com.epam.lstrsum.configuration;

import com.epam.lstrsum.Application;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Arrays;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

    @Bean
    @Profile("unsecured")
    public Docket apiForUnsecuredProfile() {
        return getDocketWithBaseConfig();
    }

    @Bean
    @ConditionalOnMissingBean(Docket.class)
    public Docket api() {
        return getDocketWithBaseConfig()
                .globalOperationParameters(
                        Arrays.asList(new ParameterBuilder()
                                .name("X-XSRF-TOKEN")
                                .description("Token for csrf protection. See cookie 'XSRF-TOKEN'")
                                .modelRef(new ModelRef("string"))
                                .parameterType("header")
                                .required(true)
                                .build()
                        )
                );
    }

    private Docket getDocketWithBaseConfig() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage(Application.class.getPackage().getName()))
                .paths(PathSelectors.any())
                .build();
    }
}
