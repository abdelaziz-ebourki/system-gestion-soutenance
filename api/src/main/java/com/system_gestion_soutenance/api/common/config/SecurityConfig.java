package com.system_gestion_soutenance.api.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.system_gestion_soutenance.api.auth.jwt.JwtAuthFilter;
import com.system_gestion_soutenance.api.auth.jwt.JwtTokenProvider;
import com.system_gestion_soutenance.api.user.service.UserCacheService;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
@SuppressWarnings("PMD")

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

	private final ObjectMapper objectMapper;

	public SecurityConfig(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Bean
	JwtAuthFilter jwtAuthFilter(JwtTokenProvider jwtTokenProvider, UserCacheService userCacheService) {
		return new JwtAuthFilter(jwtTokenProvider, userCacheService);
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowedOrigins(List.of("http://localhost:5173", "http://localhost:4173"));
		config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
		config.setAllowedHeaders(List.of("*"));
		config.setAllowCredentials(true);
		config.setMaxAge(3600L);
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);
		return source;
	}

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter) throws Exception {
		http.cors(cors -> cors.configurationSource(corsConfigurationSource())).csrf(csrf -> csrf.disable())
				.headers(headers -> headers.frameOptions(o -> o.disable()))
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.exceptionHandling(ex -> ex.authenticationEntryPoint((request, response, authException) -> {
					response.setContentType("application/json;charset=UTF-8");
					response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
					objectMapper.writeValue(response.getWriter(),
							Map.of("message", "Identifiants invalides (E-mail ou mot de passe incorrect)"));
				}).accessDeniedHandler((request, response, accessDeniedException) -> {
					response.setContentType("application/json;charset=UTF-8");
					response.setStatus(HttpServletResponse.SC_FORBIDDEN);
					objectMapper.writeValue(response.getWriter(), Map.of("message", "Acces refuse"));
				}))
				.authorizeHttpRequests(auth -> auth.requestMatchers("/api/login").permitAll()
						.requestMatchers("/api/auth/**").permitAll().requestMatchers("/h2-console/**").permitAll()
						.requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/v3/api-docs.yaml", "/v3/api-docs.yml")
						.permitAll().requestMatchers("/actuator/health").permitAll()
						.requestMatchers("/api/admin/rooms/**").hasAnyRole("ADMIN", "COORDINATOR")
						.requestMatchers("/api/admin/**").hasRole("ADMIN").requestMatchers("/api/coordinator/**")
						.hasAnyRole("ADMIN", "COORDINATOR").requestMatchers("/api/teacher/**").hasRole("TEACHER")
						.requestMatchers("/api/student/**").hasRole("STUDENT").requestMatchers("/api/notifications/**")
						.authenticated().anyRequest().authenticated())
				.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
