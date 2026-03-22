package com.ctse.hospitalservice;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("public")
                .pathsToMatch("/api/**")
                .build();
    }

    @Bean
    public GroupedOpenApi hospitalApi() {
        return GroupedOpenApi.builder()
                .group("hospitals")
                .pathsToMatch("/api/hospitals/**")
                .build();
    }
}
