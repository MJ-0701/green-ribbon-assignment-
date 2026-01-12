package com.example.greenribboncalimassignment.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("놓친 보험금 청구 대행 시스템 API")
                        .description("사용자 진료 기록 기반 보험금 계산 및 청구 대행 API 명세서")
                        .version("v1.0.0"));
    }
}
