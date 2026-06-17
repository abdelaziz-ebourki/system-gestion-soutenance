package com.system_gestion_soutenance.api.common.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;
@SuppressWarnings("PMD")

@Configuration
@OpenAPIDefinition(info = @Info(title = "Defense Management System API", version = "1.0.0", description = "RESTful API for managing final-year project defense sessions.", contact = @Contact(name = "Backend Team", email = "dev@univ-h2.ma")), servers = @Server(url = "http://localhost:8080", description = "Local development server"), security = @SecurityRequirement(name = "jwt_token"))
@SecurityScheme(name = "jwt_token", type = SecuritySchemeType.APIKEY, in = SecuritySchemeIn.COOKIE, description = "JWT token set as an HTTP-only cookie via POST /api/auth/login")
public class OpenApiConfig {
}
