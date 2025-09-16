package team.shdsesc.stocksimul.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI openAPI() {
        SecurityScheme bearerAuth = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)     // ← HTTP 타입
                .scheme("bearer")                   // ← bearer 스킴
                .bearerFormat("JWT")                // (선택) 표시용
                .in(SecurityScheme.In.HEADER)
                .name("Authorization");

        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList("BearerAuth");             // ← 이름 일치

        return new OpenAPI()
                .info(new Info()
                        .title("Todolist API")
                        .description("Todolist Application API Documentation")
                        .version("v1.0"))
                .addSecurityItem(securityRequirement)
                .components(new Components().addSecuritySchemes("BearerAuth", bearerAuth));
    }

}
