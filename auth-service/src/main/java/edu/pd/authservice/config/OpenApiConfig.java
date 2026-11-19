package edu.pd.authservice.config;

import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.*;

@Configuration
public class OpenApiConfig {
    @Bean
    OpenAPI authOpenApi() {
        return new OpenAPI().info(new Info().title("HCC AUTH API").version("1.0"))
                .components(new Components()
                        .addSecuritySchemes("serviceBearer", new SecurityScheme().type(SecurityScheme.Type.HTTP)
                                .scheme("bearer").bearerFormat("JWT"))
                        .addSecuritySchemes("sessionCookie", new SecurityScheme().type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.COOKIE).name("AUTHSESSION")));
    }
}
